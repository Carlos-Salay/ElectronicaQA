package com.tienda.electronica.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tienda.electronica.entity.Cliente;
import com.tienda.electronica.entity.DetallePedido;
import com.tienda.electronica.entity.Pedido;
import com.tienda.electronica.exceptions.ClienteNotFoundException;
import com.tienda.electronica.exceptions.PedidoNotFoundException;
import com.tienda.electronica.repository.ClienteRepository;
import com.tienda.electronica.repository.PedidoRepository;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private static final BigDecimal TASA_IMPUESTO = new BigDecimal("0.12"); // 12% IVA
    private static final BigDecimal COSTO_ENVIO_ESTANDAR = new BigDecimal("15000.00");
    private static final BigDecimal MONTO_ENVIO_GRATIS = new BigDecimal("200000.00");

    public List<Pedido> obtenerTodos() {
        return pedidoRepository.findAll();
    }

    public Optional<Pedido> obtenerPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public Pedido crear(Pedido pedido) {
        calcularTotales(pedido);
        pedido.setFechaPedido(LocalDateTime.now());
        Long clienteId = pedido.getCliente().getId();
        pedido.getDetalles().forEach(detalle -> detalle.setPedido(pedido));
        Cliente cliente = clienteRepository.findById(
                clienteId)
                .orElseThrow(() -> new ClienteNotFoundException(clienteId));
        pedido.setCliente(cliente);
        return pedidoRepository.save(pedido);
    }

    public Pedido actualizar(Long id, Pedido pedidoActualizado) {
        return pedidoRepository.findById(id)
                .map(pedido -> {
                    pedido.setEstado(pedidoActualizado.getEstado());
                    pedido.setMetodoPago(pedidoActualizado.getMetodoPago());
                    pedido.setDireccionEnvio(pedidoActualizado.getDireccionEnvio());
                    pedido.setNumeroSeguimiento(pedidoActualizado.getNumeroSeguimiento());
                    pedido.setFechaEnvio(pedidoActualizado.getFechaEnvio());
                    pedido.setFechaEntregaEstimada(pedidoActualizado.getFechaEntregaEstimada());
                    pedido.setObservaciones(pedidoActualizado.getObservaciones());

                    if (pedidoActualizado.getDetalles() != null) {
                        pedido.setDetalles(pedidoActualizado.getDetalles());
                        calcularTotales(pedido);
                    }
                    return pedidoRepository.save(pedido);
                })
                .orElseThrow(() -> new PedidoNotFoundException(id));
    }

    public void eliminar(Long id) {
        if (!pedidoRepository.existsById(id)) {
            throw new PedidoNotFoundException(id);
        }
        pedidoRepository.deleteById(id);
    }

    public List<Pedido> obtenerPorCliente(Long clienteId) {
        return pedidoRepository.findByClienteIdOrderByFechaPedidoDesc(clienteId);
    }

    public List<Pedido> obtenerPorEstado(Pedido.EstadoPedido estado) {
        return pedidoRepository.findByEstado(estado);
    }

    public List<Pedido> obtenerPorMetodoPago(Pedido.MetodoPago metodoPago) {
        return pedidoRepository.findByMetodoPago(metodoPago);
    }

    public List<Pedido> obtenerPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        return pedidoRepository.findByFechaPedidoBetween(fechaInicio, fechaFin);
    }

    public List<Pedido> buscarPorNumeroSeguimiento(String numeroSeguimiento) {
        return pedidoRepository.findByNumeroSeguimiento(numeroSeguimiento);
    }

    private void calcularTotales(Pedido pedido) {
        if (pedido.getDetalles() != null && !pedido.getDetalles().isEmpty()) {
            BigDecimal subtotal = pedido.getDetalles().stream()
                    .map(DetallePedido::getSubtotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal impuestos = subtotal.multiply(TASA_IMPUESTO);

            BigDecimal costoEnvio = COSTO_ENVIO_ESTANDAR;
            if (pedido.getCliente() != null &&
                    (Boolean.TRUE.equals(pedido.getCliente().isClientePremium()) ||
                            subtotal.compareTo(MONTO_ENVIO_GRATIS) >= 0)) {
                costoEnvio = BigDecimal.ZERO;
            }

            BigDecimal total = subtotal.add(impuestos).add(costoEnvio);

            pedido.setSubtotal(subtotal);
            pedido.setImpuestos(impuestos);
            pedido.setCostoEnvio(costoEnvio);
            pedido.setTotal(total);
        }
    }

}
