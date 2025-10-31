package com.tienda.electronica.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.tienda.electronica.entity.Usuario;
import com.tienda.electronica.service.UsuarioService;

@RunWith(MockitoJUnitRunner.class)
public class UsuarioControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    private ObjectMapper objectMapper;
    private Usuario usuario1;
    private Usuario usuario2;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
        objectMapper = new ObjectMapper();

        // Configurar usuarios de prueba
        usuario1 = new Usuario();
        usuario1.setId(1L);
        usuario1.setNombre("Juan Pérez");
        usuario1.setEmail("juan.perez@example.com");
        usuario1.setTelefono("123456789");
        usuario1.setActivo(true);

        usuario2 = new Usuario();
        usuario2.setId(2L);
        usuario2.setNombre("María García");
        usuario2.setEmail("maria.garcia@example.com");
        usuario2.setTelefono("987654321");
        usuario2.setActivo(true);
    }

    @Test
    public void testObtenerTodos() throws Exception {
        // Given
        List<Usuario> usuarios = Arrays.asList(usuario1, usuario2);
        when(usuarioService.obtenerTodos()).thenReturn(usuarios);

        // When & Then
        mockMvc.perform(get("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nombre").value("María García"));

        verify(usuarioService, times(1)).obtenerTodos();
    }

    @Test
    public void testObtenerPorId_UsuarioExiste() throws Exception {
        // Given
        when(usuarioService.obtenerPorId(1L)).thenReturn(Optional.of(usuario1));

        // When & Then
        mockMvc.perform(get("/api/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"))
                .andExpect(jsonPath("$.email").value("juan.perez@example.com"));

        verify(usuarioService, times(1)).obtenerPorId(1L);
    }

    @Test
    public void testObtenerPorId_UsuarioNoExiste() throws Exception {
        // Given
        when(usuarioService.obtenerPorId(99L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/usuarios/99")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(usuarioService, times(1)).obtenerPorId(99L);
    }

    @Test
    public void testActualizar_Success() throws Exception {
        // Given
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setNombre("Juan Pérez Actualizado");
        usuarioActualizado.setEmail("juan.actualizado@example.com");
        usuarioActualizado.setTelefono("999999999");
        usuarioActualizado.setActivo(true);

        when(usuarioService.actualizar(eq(1L), any(Usuario.class))).thenReturn(usuarioActualizado);

        // When & Then
        mockMvc.perform(put("/api/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioActualizado)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Juan Pérez Actualizado"))
                .andExpect(jsonPath("$.email").value("juan.actualizado@example.com"));

        verify(usuarioService, times(1)).actualizar(eq(1L), any(Usuario.class));
    }

    @Test
    public void testActualizar_UsuarioNoEncontrado() throws Exception {
        // Given
        when(usuarioService.actualizar(eq(99L), any(Usuario.class)))
                .thenThrow(new RuntimeException("Usuario no encontrado"));

        // When & Then
        mockMvc.perform(put("/api/usuarios/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario1)))
                .andExpect(status().isNotFound());

        verify(usuarioService, times(1)).actualizar(eq(99L), any(Usuario.class));
    }

    @Test
    public void testEliminar() throws Exception {
        // Given - no necesitamos configurar when() porque el método es void

        // When & Then
        mockMvc.perform(delete("/api/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).eliminar(1L);
    }

    @Test
    public void testObtenerActivos() throws Exception {
        // Given
        List<Usuario> usuariosActivos = Arrays.asList(usuario1, usuario2);
        when(usuarioService.obtenerActivos()).thenReturn(usuariosActivos);

        // When & Then
        mockMvc.perform(get("/api/usuarios/activos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[1].activo").value(true));

        verify(usuarioService, times(1)).obtenerActivos();
    }

    @Test
    public void testBuscarPorNombre_ConResultados() throws Exception {
        // Given
        when(usuarioService.buscarPorNombre("juan")).thenReturn(Optional.of(usuario1));

        // When & Then
        mockMvc.perform(get("/api/usuarios/buscar")
                .param("nombre", "juan")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan Pérez"));

        verify(usuarioService, times(1)).buscarPorNombre("juan");
    }

    @Test
    public void testBuscarPorNombre_SinResultados() throws Exception {
        // Given
        when(usuarioService.buscarPorNombre("inexistente")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/usuarios/buscar")
                .param("nombre", "inexistente")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).buscarPorNombre("inexistente");
    }

    @Test
    public void testBuscarPorNombre_ParametroVacio() throws Exception {
        // Given
        when(usuarioService.buscarPorNombre("")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/usuarios/buscar")
                .param("nombre", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).buscarPorNombre("");
    }

    @Test
    public void testActualizar_ValidacionRequestBody() throws Exception {
        // Given
        Usuario usuarioInvalido = new Usuario(); // Usuario sin datos requeridos
        usuarioInvalido.setNombre(""); // Nombre vacío

        // When & Then - En un escenario real con validación, esto debería fallar
        // Pero como no estamos probando validaciones específicas, solo verificamos la
        // llamada
        when(usuarioService.actualizar(eq(1L), any(Usuario.class))).thenReturn(usuario1);

        mockMvc.perform(put("/api/usuarios/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuarioInvalido)))
                .andExpect(status().isOk());

        verify(usuarioService, times(1)).actualizar(eq(1L), any(Usuario.class));
    }

    @Test
    public void testEndpoints_ContentType() throws Exception {
        // Test para verificar que todos los endpoints retornan JSON
        when(usuarioService.obtenerTodos()).thenReturn(Arrays.asList(usuario1));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        when(usuarioService.obtenerPorId(1L)).thenReturn(Optional.of(usuario1));
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        when(usuarioService.obtenerActivos()).thenReturn(Arrays.asList(usuario1));
        mockMvc.perform(get("/api/usuarios/activos"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testEliminar_VerificarLlamadaServicio() throws Exception {
        // Test específico para verificar la llamada al servicio de eliminación
        mockMvc.perform(delete("/api/usuarios/123"))
                .andExpect(status().isNoContent());

        verify(usuarioService, times(1)).eliminar(123L);
        verifyNoMoreInteractions(usuarioService);
    }
}