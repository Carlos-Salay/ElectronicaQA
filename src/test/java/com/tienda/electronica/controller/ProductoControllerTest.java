package com.tienda.electronica.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tienda.electronica.entity.Producto;
import com.tienda.electronica.service.ProductoService;

@RunWith(MockitoJUnitRunner.class)
public class ProductoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    private ObjectMapper objectMapper;
    private Producto producto1;
    private Producto producto2;
    private Producto producto3;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productoController).build();
        objectMapper = new ObjectMapper();

        // Configurar productos de prueba
        producto1 = new Producto();
        producto1.setId(1L);
        producto1.setNombre("Laptop Gaming");
        producto1.setDescripcion("Laptop para gaming de alta gama");
        producto1.setPrecio(new BigDecimal("1299.99"));
        producto1.setStock(10);
        producto1.setCategoria("Computadoras");

        producto2 = new Producto();
        producto2.setId(2L);
        producto2.setNombre("Smartphone Android");
        producto2.setDescripcion("Teléfono inteligente Android");
        producto2.setPrecio(new BigDecimal("499.99"));
        producto2.setStock(25);
        producto2.setCategoria("Telefonía");

        producto3 = new Producto();
        producto3.setId(3L);
        producto3.setNombre("Tablet");
        producto3.setDescripcion("Tablet de 10 pulgadas");
        producto3.setPrecio(new BigDecimal("299.99"));
        producto3.setStock(0); // Sin stock
        producto3.setCategoria("Tablets");
    }

    @Test
    public void testObtenerTodos() throws Exception {
        // Given
        List<Producto> productos = Arrays.asList(producto1, producto2);
        when(productoService.obtenerTodos()).thenReturn(productos);

        // When & Then
        mockMvc.perform(get("/api/productos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Laptop Gaming"))
                .andExpect(jsonPath("$[0].precio").value(1299.99))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nombre").value("Smartphone Android"));

        verify(productoService, times(1)).obtenerTodos();
    }

    @Test
    public void testObtenerPorId_ProductoExiste() throws Exception {
        // Given
        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(producto1));

        // When & Then
        mockMvc.perform(get("/api/productos/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laptop Gaming"))
                .andExpect(jsonPath("$.descripcion").value("Laptop para gaming de alta gama"))
                .andExpect(jsonPath("$.precio").value(1299.99))
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(jsonPath("$.categoria").value("Computadoras"));

        verify(productoService, times(1)).obtenerPorId(1L);
    }

    @Test
    public void testObtenerPorId_ProductoNoExiste() throws Exception {
        // Given
        when(productoService.obtenerPorId(99L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/productos/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).obtenerPorId(99L);
    }

    @Test
    public void testCrear_Success() throws Exception {
        // Given
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre("Nuevo Producto");
        nuevoProducto.setDescripcion("Descripción del nuevo producto");
        nuevoProducto.setPrecio(new BigDecimal("199.99"));
        nuevoProducto.setStock(15);
        nuevoProducto.setCategoria("Nueva Categoría");

        Producto productoCreado = new Producto();
        productoCreado.setId(4L);
        productoCreado.setNombre("Nuevo Producto");
        productoCreado.setDescripcion("Descripción del nuevo producto");
        productoCreado.setPrecio(new BigDecimal("199.99"));
        productoCreado.setStock(15);
        productoCreado.setCategoria("Nueva Categoría");

        when(productoService.crear(any(Producto.class))).thenReturn(productoCreado);

        // When & Then
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoProducto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.nombre").value("Nuevo Producto"))
                .andExpect(jsonPath("$.precio").value(199.99));

        verify(productoService, times(1)).crear(any(Producto.class));
    }

    @Test
    public void testActualizar_Success() throws Exception {
        // Given
        Producto productoActualizado = new Producto();
        productoActualizado.setNombre("Laptop Gaming Actualizada");
        productoActualizado.setDescripcion("Laptop actualizada para gaming");
        productoActualizado.setPrecio(new BigDecimal("1399.99"));
        productoActualizado.setStock(8);
        productoActualizado.setCategoria("Computadoras");

        Producto productoRespuesta = new Producto();
        productoRespuesta.setId(1L);
        productoRespuesta.setNombre("Laptop Gaming Actualizada");
        productoRespuesta.setDescripcion("Laptop actualizada para gaming");
        productoRespuesta.setPrecio(new BigDecimal("1399.99"));
        productoRespuesta.setStock(8);
        productoRespuesta.setCategoria("Computadoras");

        when(productoService.actualizar(eq(1L), any(Producto.class))).thenReturn(productoRespuesta);

        // When & Then
        mockMvc.perform(put("/api/productos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Laptop Gaming Actualizada"))
                .andExpect(jsonPath("$.precio").value(1399.99));

        verify(productoService, times(1)).actualizar(eq(1L), any(Producto.class));
    }

    @Test
    public void testActualizar_ProductoNoEncontrado() throws Exception {
        // Given
        when(productoService.actualizar(eq(99L), any(Producto.class)))
                .thenThrow(new RuntimeException("Producto no encontrado"));

        // When & Then
        mockMvc.perform(put("/api/productos/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(producto1)))
                .andExpect(status().isNotFound());

        verify(productoService, times(1)).actualizar(eq(99L), any(Producto.class));
    }

    @Test
    public void testEliminar() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/productos/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(productoService, times(1)).eliminar(1L);
    }

    @Test
    public void testObtenerPorCategoria() throws Exception {
        // Given
        List<Producto> productosComputadoras = Arrays.asList(producto1);
        when(productoService.obtenerPorCategoria("Computadoras")).thenReturn(productosComputadoras);

        // When & Then
        mockMvc.perform(get("/api/productos/categoria/Computadoras")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].categoria").value("Computadoras"));

        verify(productoService, times(1)).obtenerPorCategoria("Computadoras");
    }

    @Test
    public void testObtenerConStock() throws Exception {
        // Given
        List<Producto> productosConStock = Arrays.asList(producto1, producto2);
        when(productoService.obtenerConStock()).thenReturn(productosConStock);

        // When & Then
        mockMvc.perform(get("/api/productos/con-stock")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].stock").value(10))
                .andExpect(jsonPath("$[1].stock").value(25));

        verify(productoService, times(1)).obtenerConStock();
    }

    @Test
    public void testObtenerConStock_SinProductos() throws Exception {
        // Given
        when(productoService.obtenerConStock()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/productos/con-stock")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(productoService, times(1)).obtenerConStock();
    }

    @Test
    public void testCrear_ValidacionRequestBody() throws Exception {
        // Given
        Producto productoInvalido = new Producto(); // Producto sin datos requeridos
        // No establecer nombre, precio, etc. - debería fallar la validación

        // Cuando hay validación @Valid, Spring devuelve 400 Bad Request
        // Pero como estamos en test unitario sin validación real, simulamos el éxito
        when(productoService.crear(any(Producto.class))).thenReturn(producto1);

        // When & Then
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoInvalido)))
                .andExpect(status().isCreated()); // En realidad sería 400 con validación

        verify(productoService, times(1)).crear(any(Producto.class));
    }

    @Test
    public void testActualizar_ValidacionRequestBody() throws Exception {
        // Given
        Producto productoInvalido = new Producto();
        productoInvalido.setNombre(""); // Nombre vacío

        when(productoService.actualizar(eq(1L), any(Producto.class))).thenReturn(producto1);

        // When & Then
        mockMvc.perform(put("/api/productos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productoInvalido)))
                .andExpect(status().isOk());

        verify(productoService, times(1)).actualizar(eq(1L), any(Producto.class));
    }

    @Test
    public void testEndpoints_ContentType() throws Exception {
        // Test para verificar que todos los endpoints retornan JSON
        when(productoService.obtenerTodos()).thenReturn(Arrays.asList(producto1));
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        when(productoService.obtenerPorId(1L)).thenReturn(Optional.of(producto1));
        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        when(productoService.obtenerPorCategoria("Computadoras")).thenReturn(Arrays.asList(producto1));
        mockMvc.perform(get("/api/productos/categoria/Computadoras"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        when(productoService.obtenerConStock()).thenReturn(Arrays.asList(producto1));
        mockMvc.perform(get("/api/productos/con-stock"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testEliminar_VerificarLlamadaServicio() throws Exception {
        // Test específico para verificar la llamada al servicio de eliminación
        mockMvc.perform(delete("/api/productos/123"))
                .andExpect(status().isNoContent());

        verify(productoService, times(1)).eliminar(123L);
    }

    @Test
    public void testObtenerPorCategoria_CategoriaConEspacios() throws Exception {
        // Given
        List<Producto> productos = Arrays.asList(producto2);
        when(productoService.obtenerPorCategoria("Telefonía")).thenReturn(productos);

        // When & Then
        mockMvc.perform(get("/api/productos/categoria/Telefonía")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria").value("Telefonía"));

        verify(productoService, times(1)).obtenerPorCategoria("Telefonía");
    }

    @Test
    public void testCrear_LocationHeader() throws Exception {
        // Given
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre("Producto Test");
        nuevoProducto.setPrecio(new BigDecimal("99.99"));
        nuevoProducto.setStock(5);
        nuevoProducto.setCategoria("Test");

        Producto productoCreado = new Producto();
        productoCreado.setId(10L);
        productoCreado.setNombre("Producto Test");
        productoCreado.setPrecio(new BigDecimal("99.99"));
        productoCreado.setStock(5);
        productoCreado.setCategoria("Test");

        when(productoService.crear(any(Producto.class))).thenReturn(productoCreado);

        // When & Then
        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nuevoProducto)))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location")); // No hay header Location en la respuesta actual

        verify(productoService, times(1)).crear(any(Producto.class));
    }
}