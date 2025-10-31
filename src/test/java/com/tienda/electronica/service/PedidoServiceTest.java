package com.tienda.electronica.service;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.tienda.electronica.entity.Cliente;
import com.tienda.electronica.entity.DetallePedido;
import com.tienda.electronica.entity.Pedido;
import com.tienda.electronica.entity.Pedido.EstadoPedido;
import com.tienda.electronica.entity.Pedido.MetodoPago;
import com.tienda.electronica.entity.Producto;
import com.tienda.electronica.exceptions.ClienteNotFoundException;
import com.tienda.electronica.exceptions.PedidoNotFoundException;
import com.tienda.electronica.repository.ClienteRepository;
import com.tienda.electronica.repository.PedidoRepository;
import com.tienda.electronica.repository.ProductoRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PedidoServiceTest {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private Cliente cliente;

    @Before
    public void setUp() {
        pedidoRepository.deleteAll();
        clienteRepository.deleteAll();

        cliente = Cliente.builder()
                .nombre("Daniela")
                .apellidos("López")
                .email("daniela@example.com")
                .telefono("555555")
                .direccion("Zona 7")
                .ciudad("Guatemala")
                .codigoPostal("01007")
                .activo(true)
                .clientePremium(false)
                .build();

        cliente = clienteRepository.save(cliente);
    }

    private Pedido crearPedidoBase(BigDecimal precioUnitario, int cantidad) {
        var newProducto = Producto.builder()
                .nombre("Test")
                .descripcion("Test")
                .precio(new BigDecimal("100"))
                .stock(1)
                .categoria("Test")
                .fechaCreacion(LocalDateTime.now())
                .build();

        // 🔹 Guardar el producto antes de usarlo en el detalle
        newProducto = productoRepository.save(newProducto);

        DetallePedido detalle = DetallePedido.builder()
                .precioUnitario(precioUnitario)
                .cantidad(cantidad)
                .subtotal(precioUnitario.multiply(BigDecimal.valueOf(cantidad)))
                .producto(newProducto) // ya persistido
                .build();

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .estado(EstadoPedido.PENDIENTE)
                .metodoPago(MetodoPago.TARJETA_DEBITO)
                .direccionEnvio("Zona 7")
                .detalles(Arrays.asList(detalle))
                .build();

        detalle.setPedido(pedido);

        pedidoRepository.save(pedido);

        return pedido;
    }

    @Test
    public void testCrearPedido() {
        Pedido pedido = crearPedidoBase(new BigDecimal("50000"), 1);
        Pedido creado = pedidoService.crear(pedido);

        assertNotNull(creado.getId());
        assertEquals(cliente.getId(), creado.getCliente().getId());
        assertEquals(new BigDecimal("50000"), creado.getSubtotal());
        assertEquals(new BigDecimal("6000.00"), creado.getImpuestos());
        assertEquals(new BigDecimal("15000.00"), creado.getCostoEnvio());
        assertEquals(new BigDecimal("71000.00"), creado.getTotal());
        assertNotNull(creado.getFechaPedido());
    }

    @Test
    public void testCrearPedidoConEnvioGratisPorMonto() {
        Pedido pedido = crearPedidoBase(new BigDecimal("200000"), 1);
        Pedido creado = pedidoService.crear(pedido);

        assertEquals(BigDecimal.ZERO, creado.getCostoEnvio());
        assertTrue(creado.getTotal().compareTo(new BigDecimal("224000.00")) == 0);
    }

    @Test
    public void testCrearPedidoConClientePremium() {
        cliente.setClientePremium(true);
        clienteRepository.save(cliente);

        Pedido pedido = crearPedidoBase(new BigDecimal("10000"), 1);
        Pedido creado = pedidoService.crear(pedido);

        assertEquals(BigDecimal.ZERO, creado.getCostoEnvio());
    }

    @Test(expected = ClienteNotFoundException.class)
    public void testCrearPedidoConClienteInexistente() {
        Pedido pedido = crearPedidoBase(new BigDecimal("10000"), 1);
        pedido.getCliente().setId(999L); // Cliente no existente
        pedidoService.crear(pedido);
    }

    @Test
    public void testActualizarPedido() {
        Pedido pedido = pedidoService.crear(crearPedidoBase(new BigDecimal("50000"), 1));
        pedido.setEstado(EstadoPedido.ENVIADO);
        pedido.setObservaciones("Empaquetado con cuidado");

        Pedido actualizado = pedidoService.actualizar(pedido.getId(), pedido);
        assertEquals(EstadoPedido.ENVIADO, actualizado.getEstado());
        assertEquals("Empaquetado con cuidado", actualizado.getObservaciones());
    }

    @Test(expected = PedidoNotFoundException.class)
    public void testActualizarPedidoInexistente() {
        Pedido pedido = crearPedidoBase(new BigDecimal("10000"), 1);
        pedidoService.actualizar(999L, pedido);
    }

    @Test
    public void testEliminarPedido() {
        Pedido pedido = pedidoService.crear(crearPedidoBase(new BigDecimal("10000"), 1));
        Long id = pedido.getId();
        pedidoService.eliminar(id);
        assertFalse(pedidoRepository.existsById(id));
    }

    @Test(expected = PedidoNotFoundException.class)
    public void testEliminarPedidoInexistente() {
        pedidoService.eliminar(999L);
    }

    @Test
    public void testObtenerPorCliente() {
        Pedido pedido = pedidoService.crear(crearPedidoBase(new BigDecimal("50000"), 1));
        List<Pedido> pedidos = pedidoService.obtenerPorCliente(cliente.getId());
        assertEquals(1, pedidos.size());
        assertEquals(pedido.getId(), pedidos.get(0).getId());
    }

    @Test
    public void testObtenerPorEstado() {
        Pedido pedido = pedidoService.crear(crearPedidoBase(new BigDecimal("50000"), 1));
        List<Pedido> pedidos = pedidoService.obtenerPorEstado(EstadoPedido.PENDIENTE);
        assertTrue(pedidos.stream().anyMatch(p -> p.getId().equals(pedido.getId())));
    }

    @Test
    public void testObtenerPorMetodoPago() {
        Pedido pedido = pedidoService.crear(crearPedidoBase(new BigDecimal("50000"), 1));
        List<Pedido> pedidos = pedidoService.obtenerPorMetodoPago(MetodoPago.TARJETA_DEBITO);
        assertEquals(1, pedidos.size());
    }

    @Test
    public void testObtenerPorRangoFechas() {
        Pedido pedido = pedidoService.crear(crearPedidoBase(new BigDecimal("50000"), 1));
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fin = LocalDateTime.now().plusDays(1);
        List<Pedido> pedidos = pedidoService.obtenerPorRangoFechas(inicio, fin);
        assertTrue(pedidos.stream().anyMatch(p -> p.getId().equals(pedido.getId())));
    }

    @Test
    public void testObtenerTodos() {
        pedidoService.crear(crearPedidoBase(new BigDecimal("10000"), 1));
        pedidoService.crear(crearPedidoBase(new BigDecimal("20000"), 1));
        List<Pedido> pedidos = pedidoService.obtenerTodos();
        assertEquals(2, pedidos.size());
    }

    @Test
    public void testObtenerPorId() {
        Pedido pedido = pedidoService.crear(crearPedidoBase(new BigDecimal("50000"), 1));
        Optional<Pedido> resultado = pedidoService.obtenerPorId(pedido.getId());
        assertTrue(resultado.isPresent());
        assertEquals(pedido.getId(), resultado.get().getId());
    }
}
