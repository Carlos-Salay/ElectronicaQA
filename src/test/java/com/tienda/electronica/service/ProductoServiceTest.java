package com.tienda.electronica.service;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.tienda.electronica.entity.Producto;
import com.tienda.electronica.exceptions.ProductoNotFoundException;
import com.tienda.electronica.repository.ProductoRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductoServiceTest {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private ProductoRepository productoRepository;

    private Producto productoBase;

    @Before
    public void setUp() {

        productoBase = Producto.builder()
                .nombre("Laptop")
                .descripcion("Laptop de prueba")
                .precio(new BigDecimal("5000.00"))
                .stock(10)
                .categoria("Computadoras")
                .build();

        productoBase = productoRepository.save(productoBase);
    }

    @Test
    public void testObtenerPorId() {
        Optional<Producto> producto = productoService.obtenerPorId(productoBase.getId());
        assertTrue(producto.isPresent());
        assertEquals("Laptop", producto.get().getNombre());
    }

    @Test
    public void testCrearProducto() {
        Producto nuevo = Producto.builder()
                .nombre("Teclado")
                .descripcion("Teclado mecánico RGB")
                .precio(new BigDecimal("250.00"))
                .stock(20)
                .categoria("Accesorios")
                .build();

        Producto creado = productoService.crear(nuevo);

        assertNotNull(creado.getId());
        assertEquals("Teclado", creado.getNombre());
        assertEquals("Accesorios", creado.getCategoria());
        assertNotNull(creado.getFechaCreacion());
    }

    @Test
    public void testActualizarProducto() {
        Producto actualizado = Producto.builder()
                .nombre("Laptop Gamer")
                .descripcion("Laptop de alto rendimiento")
                .precio(new BigDecimal("9000.00"))
                .stock(5)
                .categoria("Gaming")
                .build();

        Producto resultado = productoService.actualizar(productoBase.getId(), actualizado);

        assertEquals("Laptop Gamer", resultado.getNombre());
        assertEquals("Gaming", resultado.getCategoria());
        assertEquals(new BigDecimal("9000.00"), resultado.getPrecio());
    }

    @Test(expected = ProductoNotFoundException.class)
    public void testActualizarProductoInexistente() {
        Producto p = Producto.builder().nombre("Fake").build();
        productoService.actualizar(999L, p);
    }

    @Test
    public void testEliminarProducto() {
        productoService.eliminar(productoBase.getId());
        assertFalse(productoRepository.existsById(productoBase.getId()));
    }

    @Test
    public void testObtenerConStock() {
        List<Producto> resultado = productoService.obtenerConStock();
        assertFalse(resultado.isEmpty());
        assertTrue(resultado.stream().allMatch(p -> p.getStock() > 0));
    }

}
