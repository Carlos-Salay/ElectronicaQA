package com.tienda.electronica.repository;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.tienda.electronica.entity.Producto;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ProductoRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductoRepository productoRepository;

    private Producto producto1;
    private Producto producto2;
    private Producto producto3;
    private Producto producto4;

    @Before
    public void setUp() {
        // Limpiar datos existentes
        entityManager.clear();

        // Crear productos de prueba
        producto1 = new Producto();
        producto1.setNombre("Laptop Gaming");
        producto1.setDescripcion("Laptop para gaming de alta gama");
        producto1.setPrecio(new BigDecimal("1299.99"));
        producto1.setStock(10);
        producto1.setCategoria("Computadoras");

        producto2 = new Producto();
        producto2.setNombre("Smartphone Android");
        producto2.setDescripcion("Teléfono inteligente Android");
        producto2.setPrecio(new BigDecimal("499.99"));
        producto2.setStock(25);
        producto2.setCategoria("Telefonía");

        producto3 = new Producto();
        producto3.setNombre("Tablet 10 pulgadas");
        producto3.setDescripcion("Tablet de 10 pulgadas con stylus");
        producto3.setPrecio(new BigDecimal("299.99"));
        producto3.setStock(0); // Sin stock
        producto3.setCategoria("Tablets");

        producto4 = new Producto();
        producto4.setNombre("Smartwatch Deportivo");
        producto4.setDescripcion("Reloj inteligente para deportes");
        producto4.setPrecio(new BigDecimal("199.99"));
        producto4.setStock(5);
        producto4.setCategoria("Wearables");

        // Persistir productos
        producto1 = entityManager.persist(producto1);
        producto2 = entityManager.persist(producto2);
        producto3 = entityManager.persist(producto3);
        producto4 = entityManager.persist(producto4);
        entityManager.flush();
    }

    @Test
    public void testFindByCategoriaIgnoreCase() {
        // When
        List<Producto> resultadoComputadoras = productoRepository.findByCategoriaIgnoreCase("computadoras");
        List<Producto> resultadoCOMPUTADORAS = productoRepository.findByCategoriaIgnoreCase("COMPUTADORAS");
        List<Producto> resultadoTelefonia = productoRepository.findByCategoriaIgnoreCase("telefonía");
        List<Producto> resultadoInexistente = productoRepository.findByCategoriaIgnoreCase("inexistente");

        // Then
        assertEquals("Debe encontrar 1 producto en categoría computadoras (case insensitive)", 1,
                resultadoComputadoras.size());
        assertEquals("Debe encontrar 1 producto en categoría COMPUTADORAS (case insensitive)", 1,
                resultadoCOMPUTADORAS.size());
        assertEquals("Debe encontrar 1 producto en categoría telefonía", 1, resultadoTelefonia.size());
        assertTrue("No debe encontrar productos en categoría inexistente", resultadoInexistente.isEmpty());

        assertEquals("El producto debe ser la laptop", "Laptop Gaming", resultadoComputadoras.get(0).getNombre());
        assertEquals("La categoría debe coincidir", "Computadoras", resultadoComputadoras.get(0).getCategoria());
    }

    @Test
    public void testFindByCategoriaIgnoreCase_MultiplesProductosMismaCategoria() {
        // Given - Agregar otro producto en la misma categoría
        Producto productoExtra = new Producto();
        productoExtra.setNombre("Laptop Business");
        productoExtra.setDescripcion("Laptop para negocios");
        productoExtra.setPrecio(new BigDecimal("899.99"));
        productoExtra.setStock(8);
        productoExtra.setCategoria("Computadoras");

        entityManager.persist(productoExtra);
        entityManager.flush();

        // When
        List<Producto> resultado = productoRepository.findByCategoriaIgnoreCase("computadoras");

        // Then
        assertEquals("Debe encontrar 2 productos en categoría computadoras", 2, resultado.size());
        assertTrue("Debe contener la laptop gaming",
                resultado.stream().anyMatch(p -> "Laptop Gaming".equals(p.getNombre())));
        assertTrue("Debe contener la laptop business",
                resultado.stream().anyMatch(p -> "Laptop Business".equals(p.getNombre())));
    }

    @Test
    public void testFindByStockGreaterThan() {
        // When
        List<Producto> resultadoStockMayorCero = productoRepository.findByStockGreaterThan(0);
        List<Producto> resultadoStockMayorCinco = productoRepository.findByStockGreaterThan(5);
        List<Producto> resultadoStockMayorCien = productoRepository.findByStockGreaterThan(100);

        // Then
        assertEquals("Debe encontrar 3 productos con stock mayor a 0", 3, resultadoStockMayorCero.size());
        assertEquals("Debe encontrar 2 productos con stock mayor a 5", 2, resultadoStockMayorCinco.size());
        assertTrue("No debe encontrar productos con stock mayor a 100", resultadoStockMayorCien.isEmpty());

        // Verificar que no incluye productos con stock 0 o inactivos
        assertTrue("No debe incluir productos con stock 0",
                resultadoStockMayorCero.stream().allMatch(p -> p.getStock() > 0));

    }

    @Test
    public void testFindByStockGreaterThan_StockExacto() {
        // When
        List<Producto> resultadoStockMayorDiez = productoRepository.findByStockGreaterThan(10);
        List<Producto> resultadoStockMayorNueve = productoRepository.findByStockGreaterThan(9);

        // Then
        assertEquals("Debe encontrar 1 producto con stock mayor a 10", 1, resultadoStockMayorDiez.size());
        assertEquals("Debe encontrar 2 productos con stock mayor a 9", 2, resultadoStockMayorNueve.size());
        assertEquals("El producto con stock mayor a 10 debe ser el smartphone",
                "Smartphone Android", resultadoStockMayorDiez.get(0).getNombre());
    }

    @Test
    public void testFindByNombreContainingIgnoreCase() {
        // When
        List<Producto> resultadoLaptop = productoRepository.findByNombreContainingIgnoreCase("laptop");
        List<Producto> resultadoLAPTOP = productoRepository.findByNombreContainingIgnoreCase("LAPTOP");
        List<Producto> resultadoTablet = productoRepository.findByNombreContainingIgnoreCase("tablet");
        List<Producto> resultadoSmart = productoRepository.findByNombreContainingIgnoreCase("smart");
        List<Producto> resultadoInexistente = productoRepository.findByNombreContainingIgnoreCase("inexistente");

        // Then
        assertEquals("Debe encontrar 1 producto con 'laptop' en el nombre (case insensitive)", 1,
                resultadoLaptop.size());
        assertEquals("Debe encontrar 1 producto con 'LAPTOP' en el nombre (case insensitive)", 1,
                resultadoLAPTOP.size());
        assertEquals("Debe encontrar 1 producto con 'tablet' en el nombre", 1, resultadoTablet.size());
        assertEquals("Debe encontrar 2 productos con 'smart' en el nombre", 2, resultadoSmart.size());
        assertTrue("No debe encontrar productos con texto inexistente", resultadoInexistente.isEmpty());

        assertEquals("El producto debe ser la laptop", "Laptop Gaming", resultadoLaptop.get(0).getNombre());
    }

    @Test
    public void testFindByNombreContainingIgnoreCase_TextoParcial() {
        // When
        List<Producto> resultadoAndroid = productoRepository.findByNombreContainingIgnoreCase("android");
        List<Producto> resultadoGaming = productoRepository.findByNombreContainingIgnoreCase("gaming");
        List<Producto> resultadoPulgadas = productoRepository.findByNombreContainingIgnoreCase("pulgadas");

        // Then
        assertEquals("Debe encontrar 1 producto con 'android' en el nombre", 1, resultadoAndroid.size());
        assertEquals("Debe encontrar 1 producto con 'gaming' en el nombre", 1, resultadoGaming.size());
        assertEquals("Debe encontrar 1 producto con 'pulgadas' en el nombre", 1, resultadoPulgadas.size());

        assertEquals("El producto con 'android' debe ser el smartphone",
                "Smartphone Android", resultadoAndroid.get(0).getNombre());
    }

    @Test
    public void testFindByNombreContainingIgnoreCase_TextoCorto() {
        // When
        List<Producto> resultadoA = productoRepository.findByNombreContainingIgnoreCase("a");
        List<Producto> resultadoS = productoRepository.findByNombreContainingIgnoreCase("s");

        // Then
        assertTrue("Debe encontrar productos con 'a' en el nombre", resultadoA.size() > 0);
        assertTrue("Debe encontrar productos con 's' en el nombre", resultadoS.size() > 0);
    }

    @Test
    public void testSaveAndFindById() {
        // Given
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre("Nuevo Producto");
        nuevoProducto.setDescripcion("Descripción del nuevo producto");
        nuevoProducto.setPrecio(new BigDecimal("149.99"));
        nuevoProducto.setStock(15);
        nuevoProducto.setCategoria("Nueva Categoría");

        // When
        Producto guardado = productoRepository.save(nuevoProducto);
        Producto encontrado = productoRepository.findById(guardado.getId()).orElse(null);

        // Then
        assertNotNull("Debe encontrar el producto guardado", encontrado);
        assertEquals("El nombre debe coincidir", "Nuevo Producto", encontrado.getNombre());
        assertEquals("El precio debe coincidir", new BigDecimal("149.99"), encontrado.getPrecio());
        assertEquals("El stock debe coincidir", Integer.valueOf(15), encontrado.getStock());
        assertEquals("La categoría debe coincidir", "Nueva Categoría", encontrado.getCategoria());
    }

    @Test
    public void testDelete() {
        // When
        productoRepository.deleteById(producto1.getId());
        Producto resultado = productoRepository.findById(producto1.getId()).orElse(null);

        // Then
        assertNull("No debe encontrar el producto eliminado", resultado);
    }

    @Test
    public void testFindAll() {
        // When
        List<Producto> resultado = productoRepository.findAll();

        // Then
        assertEquals("Debe encontrar todos los productos (activos e inactivos)", 4, resultado.size());
    }

    @Test
    public void testFindByCategoriaIgnoreCase_ConProductosInactivos() {
        // Given - Agregar producto inactivo en categoría existente
        Producto productoInactivo = new Producto();
        productoInactivo.setNombre("Producto Inactivo");
        productoInactivo.setDescripcion("Producto descontinuado");
        productoInactivo.setPrecio(new BigDecimal("99.99"));
        productoInactivo.setStock(0);
        productoInactivo.setCategoria("Computadoras");

        entityManager.persist(productoInactivo);
        entityManager.flush();

        // When
        List<Producto> resultado = productoRepository.findByCategoriaIgnoreCase("computadoras");

        // Then
        assertEquals("Debe ser la laptop gaming", "Laptop Gaming", resultado.get(0).getNombre());
    }
}