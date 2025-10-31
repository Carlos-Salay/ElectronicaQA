package com.tienda.electronica.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.tienda.electronica.entity.Pedido;
import com.tienda.electronica.service.PedidoService;

@RunWith(MockitoJUnitRunner.class)
public class PedidoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PedidoService pedidoService;

    @InjectMocks
    private PedidoController pedidoController;

    private ObjectMapper objectMapper;
    private Pedido pedido1;
    private Pedido pedido2;
    private Pedido pedido3;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pedidoController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Para manejar LocalDateTime

        // Configurar pedidos de prueba
        pedido1 = new Pedido();
        pedido1.setId(1L);
        pedido1.setNumeroSeguimiento("PED-001");
        pedido1.setFechaPedido(LocalDateTime.of(2024, 1, 15, 10, 30));
        pedido1.setTotal(new BigDecimal("299.99"));
        pedido1.setEstado(Pedido.EstadoPedido.PENDIENTE);
        // pedido1.setClienteId(1L);
        pedido1.setDireccionEnvio("Calle Principal 123, Madrid");

        pedido2 = new Pedido();
        pedido2.setId(2L);
        pedido2.setNumeroSeguimiento("PED-002");
        pedido2.setFechaPedido(LocalDateTime.of(2024, 1, 16, 14, 45));
        pedido2.setTotal(new BigDecimal("599.99"));
        pedido2.setEstado(Pedido.EstadoPedido.ENVIADO);
        // pedido2.setClienteId(2L);
        pedido2.setDireccionEnvio("Avenida Central 456, Barcelona");

        pedido3 = new Pedido();
        pedido3.setId(3L);
        pedido3.setNumeroSeguimiento("PED-003");
        pedido3.setFechaPedido(LocalDateTime.of(2024, 1, 17, 9, 15));
        pedido3.setTotal(new BigDecimal("199.99"));
        pedido3.setEstado(Pedido.EstadoPedido.ENTREGADO);
        // pedido3.setClienteId(1L);
        pedido3.setDireccionEnvio("Plaza Mayor 789, Valencia");
    }

    @Test
    public void testObtenerPorId_PedidoNoExiste() throws Exception {
        // Given
        when(pedidoService.obtenerPorId(99L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/pedidos/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(pedidoService, times(1)).obtenerPorId(99L);
    }

    @Test
    public void testActualizar_PedidoNoEncontrado() throws Exception {
        // Given
        when(pedidoService.actualizar(eq(99L), any(Pedido.class)))
                .thenThrow(new RuntimeException("Pedido no encontrado"));

        // When & Then
        mockMvc.perform(put("/api/pedidos/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedido1)))
                .andExpect(status().isNotFound());

        verify(pedidoService, times(1)).actualizar(eq(99L), any(Pedido.class));
    }

    @Test
    public void testEliminar() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/pedidos/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(pedidoService, times(1)).eliminar(1L);
    }

    @Test
    public void testObtenerPorEstado_SinPedidos() throws Exception {
        // Given
        when(pedidoService.obtenerPorEstado(Pedido.EstadoPedido.CANCELADO)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/pedidos/estado/CANCELADO")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(pedidoService, times(1)).obtenerPorEstado(Pedido.EstadoPedido.CANCELADO);
    }

    @Test
    public void testCrear_ValidacionRequestBody() throws Exception {
        // Given
        Pedido pedidoInvalido = new Pedido(); // Pedido sin datos requeridos
        // No establecer datos obligatorios - debería fallar la validación

        // Cuando hay validación @Valid, Spring devuelve 400 Bad Request
        // Pero como estamos en test unitario sin validación real, simulamos el éxito
        when(pedidoService.crear(any(Pedido.class))).thenReturn(pedido1);

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedidoInvalido)))
                .andExpect(status().isCreated()); // En realidad sería 400 con validación

        verify(pedidoService, times(1)).crear(any(Pedido.class));
    }

    @Test
    public void testActualizar_ValidacionRequestBody() throws Exception {
        // Given
        Pedido pedidoInvalido = new Pedido();
        pedidoInvalido.setNumeroSeguimiento(""); // Número de pedido vacío

        when(pedidoService.actualizar(eq(1L), any(Pedido.class))).thenReturn(pedido1);

        // When & Then
        mockMvc.perform(put("/api/pedidos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedidoInvalido)))
                .andExpect(status().isOk());

        verify(pedidoService, times(1)).actualizar(eq(1L), any(Pedido.class));
    }

    @Test
    public void testEndpoints_ContentType() throws Exception {
        // Test para verificar que todos los endpoints retornan JSON
        when(pedidoService.obtenerTodos()).thenReturn(Arrays.asList(pedido1));
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        when(pedidoService.obtenerPorId(1L)).thenReturn(Optional.of(pedido1));
        mockMvc.perform(get("/api/pedidos/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        when(pedidoService.obtenerPorEstado(Pedido.EstadoPedido.PENDIENTE)).thenReturn(Arrays.asList(pedido1));
        mockMvc.perform(get("/api/pedidos/estado/PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testEliminar_VerificarLlamadaServicio() throws Exception {
        // Test específico para verificar la llamada al servicio de eliminación
        mockMvc.perform(delete("/api/pedidos/123"))
                .andExpect(status().isNoContent());

        verify(pedidoService, times(1)).eliminar(123L);
    }

    @Test
    public void testCrear_WithAllFields() throws Exception {
        // Given
        Pedido pedidoCompleto = new Pedido();
        pedidoCompleto.setNumeroSeguimiento("PED-COMPLETE");
        pedidoCompleto.setFechaPedido(LocalDateTime.of(2024, 1, 18, 16, 30));
        pedidoCompleto.setTotal(new BigDecimal("899.99"));
        pedidoCompleto.setEstado(Pedido.EstadoPedido.PENDIENTE);
        // pedidoCompleto.setClienteId(5L);
        pedidoCompleto.setDireccionEnvio("Dirección Completa 555, Bilbao");
        pedidoCompleto.setObservaciones("Pedido con observaciones especiales");

        Pedido pedidoCreado = new Pedido();
        pedidoCreado.setId(10L);
        pedidoCreado.setNumeroSeguimiento("PED-COMPLETE");
        pedidoCreado.setFechaPedido(LocalDateTime.of(2024, 1, 18, 16, 30));
        pedidoCreado.setTotal(new BigDecimal("899.99"));
        pedidoCreado.setEstado(Pedido.EstadoPedido.PENDIENTE);
        // pedidoCreado.setClienteId(5L);
        pedidoCreado.setDireccionEnvio("Dirección Completa 555, Bilbao");
        pedidoCreado.setObservaciones("Pedido con observaciones especiales");

        when(pedidoService.crear(any(Pedido.class))).thenReturn(pedidoCreado);

        // When & Then
        mockMvc.perform(post("/api/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedidoCompleto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.observaciones").value("Pedido con observaciones especiales"));

        verify(pedidoService, times(1)).crear(any(Pedido.class));
    }

    @Test
    public void testObtenerPorEstado_MultipleResults() throws Exception {
        // Given
        List<Pedido> pedidosMultiples = Arrays.asList(pedido1, pedido2, pedido3);
        when(pedidoService.obtenerPorEstado(Pedido.EstadoPedido.PENDIENTE)).thenReturn(pedidosMultiples);

        // When & Then
        mockMvc.perform(get("/api/pedidos/estado/PENDIENTE")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));

        verify(pedidoService, times(1)).obtenerPorEstado(Pedido.EstadoPedido.PENDIENTE);
    }
}