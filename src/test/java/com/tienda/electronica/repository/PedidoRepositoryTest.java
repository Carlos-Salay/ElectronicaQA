package com.tienda.electronica.repository;

import static org.junit.Assert.*;
import java.time.LocalDateTime;
import java.util.List;

import com.tienda.electronica.entity.Cliente;
import com.tienda.electronica.entity.Pedido;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PedidoRepositoryTest {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private TestEntityManager entityManager; // Usar TestEntityManager en lugar de repository directo

    private Cliente cliente;
    private Pedido pedido1;
    private Pedido pedido2;

    @Before
    public void setUp() {
        // Limpiar las tablas usando TestEntityManager
        entityManager.clear();

        // Crear y persistir cliente sin establecer ID manualmente
        cliente = new Cliente();
        cliente.setNombre("Juan Pérez");
        cliente.setApellidos("García");
        cliente.setEmail("juan.perez@example.com");
        cliente.setTelefono("123456789");
        cliente.setDireccion("Calle Principal 123");
        cliente.setCiudad("Madrid");
        cliente.setActivo(true);
        cliente.setClientePremium(false);
        cliente.setCodigoPostal("123");

        // Persistir el cliente y obtener el ID generado
        cliente = entityManager.persistAndFlush(cliente);

        // Crear pedidos
        pedido1 = new Pedido();
        pedido1.setEstado(Pedido.EstadoPedido.PENDIENTE);
        pedido1.setMetodoPago(Pedido.MetodoPago.TARJETA_DEBITO);
        pedido1.setFechaPedido(LocalDateTime.now().minusDays(1));
        pedido1.setNumeroSeguimiento("ABC123");
        pedido1.setCliente(cliente);

        pedido2 = new Pedido();
        pedido2.setEstado(Pedido.EstadoPedido.ENTREGADO);
        pedido2.setMetodoPago(Pedido.MetodoPago.EFECTIVO_CONTRAENTREGA);
        pedido2.setFechaPedido(LocalDateTime.now());
        pedido2.setNumeroSeguimiento("XYZ789");
        pedido2.setCliente(cliente);

        // Persistir pedidos
        pedido1 = entityManager.persistAndFlush(pedido1);
        pedido2 = entityManager.persistAndFlush(pedido2);
    }

    @Test
    public void testFindByClienteIdOrderByFechaPedidoDesc() {
        List<Pedido> pedidos = pedidoRepository.findByClienteIdOrderByFechaPedidoDesc(cliente.getId());
        assertEquals(2, pedidos.size());
        assertTrue(pedidos.get(0).getFechaPedido().isAfter(pedidos.get(1).getFechaPedido()));
    }

    @Test
    public void testFindByEstado() {
        List<Pedido> pendientes = pedidoRepository.findByEstado(Pedido.EstadoPedido.PENDIENTE);
        assertEquals(1, pendientes.size());
        assertEquals(Pedido.EstadoPedido.PENDIENTE, pendientes.get(0).getEstado());
    }

    @Test
    public void testFindByMetodoPago() {
        List<Pedido> pagosTarjeta = pedidoRepository.findByMetodoPago(Pedido.MetodoPago.TARJETA_DEBITO);
        assertEquals(1, pagosTarjeta.size());
        assertEquals(Pedido.MetodoPago.TARJETA_DEBITO, pagosTarjeta.get(0).getMetodoPago());
    }

    @Test
    public void testFindByFechaPedidoBetween() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(2);
        LocalDateTime fin = LocalDateTime.now().plusDays(1); // Incluir margen para evitar problemas de tiempo
        List<Pedido> pedidos = pedidoRepository.findByFechaPedidoBetween(inicio, fin);
        assertEquals(2, pedidos.size());
    }

    @Test
    public void testFindByNumeroSeguimiento() {
        List<Pedido> resultado = pedidoRepository.findByNumeroSeguimiento("ABC123");
        assertEquals(1, resultado.size());
        assertEquals("ABC123", resultado.get(0).getNumeroSeguimiento());
    }

    @Test
    public void testFindByClienteIdOrderByFechaPedidoDesc_ClienteNoExiste() {
        List<Pedido> pedidos = pedidoRepository.findByClienteIdOrderByFechaPedidoDesc(999L);
        assertTrue(pedidos.isEmpty());
    }

    @Test
    public void testFindByEstado_NoExiste() {
        List<Pedido> cancelados = pedidoRepository.findByEstado(Pedido.EstadoPedido.CANCELADO);
        assertTrue(cancelados.isEmpty());
    }
}