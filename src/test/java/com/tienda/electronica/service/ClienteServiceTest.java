package com.tienda.electronica.service;

import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.tienda.electronica.entity.Cliente;
import com.tienda.electronica.repository.ClienteRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ClienteServiceTest {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    private Cliente clienteBase;

    @Before
    public void setUp() {
        clienteRepository.deleteAll();
        clienteBase = Cliente.builder()
                .nombre("Juan")
                .apellidos("Pérez")
                .email("juan@example.com")
                .telefono("5551234")
                .direccion("Av. Central 123")
                .ciudad("Guatemala")
                .codigoPostal("01001")
                .activo(true)
                .clientePremium(false)
                .build();
        clienteBase = clienteRepository.save(clienteBase);
    }

    @Test
    public void testObtenerTodos() {
        List<Cliente> clientes = clienteService.obtenerTodos();
        assertFalse(clientes.isEmpty());
        assertEquals(1, clientes.size());
    }

    @Test
    public void testObtenerPorId() {
        Optional<Cliente> cliente = clienteService.obtenerPorId(clienteBase.getId());
        assertTrue(cliente.isPresent());
        assertEquals("Juan", cliente.get().getNombre());
    }

    @Test
    public void testCrearCliente() {
        Cliente nuevo = Cliente.builder()
                .nombre("Ana")
                .apellidos("Gómez")
                .email("ana@example.com")
                .telefono("123456")
                .direccion("Zona 10")
                .ciudad("Guatemala")
                .codigoPostal("01010")
                .clientePremium(true)
                .build();

        Cliente creado = clienteService.crear(nuevo);

        assertNotNull(creado.getId());
        assertTrue(creado.isActivo());
        assertTrue(creado.isClientePremium());
        assertEquals("Ana", creado.getNombre());
    }

    @Test
    public void testActualizarCliente() {
        Cliente actualizado = Cliente.builder()
                .nombre("Juan Carlos")
                .apellidos("Pérez López")
                .email("juan.carlos@example.com")
                .telefono("987654")
                .direccion("Zona 1")
                .ciudad("Mixco")
                .codigoPostal("01002")
                .activo(true)
                .clientePremium(true)
                .build();

        Cliente resultado = clienteService.actualizar(clienteBase.getId(), actualizado);

        assertEquals("Juan Carlos", resultado.getNombre());
        assertTrue(resultado.isClientePremium());
    }

    @Test(expected = com.tienda.electronica.exceptions.ClienteNotFoundException.class)
    public void testActualizarClienteInexistente() {
        Cliente c = Cliente.builder().nombre("Fake").build();
        clienteService.actualizar(999L, c);
    }

    @Test
    public void testEliminarCliente() {
        clienteService.eliminar(clienteBase.getId());
        assertFalse(clienteRepository.existsById(clienteBase.getId()));
    }

    @Test(expected = com.tienda.electronica.exceptions.ClienteNotFoundException.class)
    public void testEliminarClienteInexistente() {
        clienteService.eliminar(999L);
    }

    @Test
    public void testObtenerActivos() {
        List<Cliente> activos = clienteService.obtenerActivos();
        assertEquals(1, activos.size());
    }

    @Test
    public void testObtenerClientesPremium() {
        clienteBase.setClientePremium(true);
        clienteRepository.save(clienteBase);

        List<Cliente> premium = clienteService.obtenerClientesPremium();
        assertEquals(1, premium.size());
    }

    @Test
    public void testObtenerPorEmail() {
        Optional<Cliente> encontrado = clienteService.obtenerPorEmail("juan@example.com");
        assertTrue(encontrado.isPresent());
        assertEquals(clienteBase.getEmail(), encontrado.get().getEmail());
    }

    @Test
    public void testBuscarPorNombre() {
        List<Cliente> resultado = clienteService.buscarPorNombre("Juan");
        assertFalse(resultado.isEmpty());
    }

    @Test
    public void testObtenerPorCiudad() {
        List<Cliente> resultado = clienteService.obtenerPorCiudad("Guatemala");
        assertEquals(1, resultado.size());
    }
}
