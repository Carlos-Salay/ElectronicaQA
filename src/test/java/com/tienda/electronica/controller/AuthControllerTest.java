package com.tienda.electronica.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tienda.electronica.entity.Usuario;
import com.tienda.electronica.request.LoginRequest;
import com.tienda.electronica.response.TokenResponse;
import com.tienda.electronica.service.AuthService;

@RunWith(MockitoJUnitRunner.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private ObjectMapper objectMapper;
    private Usuario usuario;
    private LoginRequest loginRequest;
    private TokenResponse tokenResponse;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
        objectMapper = new ObjectMapper();

        // Configurar datos de prueba
        usuario = new Usuario();
        usuario.setNombre("Juan Pérez");
        usuario.setEmail("juan.perez@example.com");
        usuario.setPassword("password123");
        usuario.setTelefono("123456789");

        loginRequest = new LoginRequest("juan.perez@example.com", "password123");

        tokenResponse = new TokenResponse("jwt-token-here", "refresh-token-here");
    }

    @Test
    public void testCrearCuenta_WithEmptyRequestBody() throws Exception {
        // Given
        when(authService.crearCuenta(any(Usuario.class))).thenReturn(tokenResponse);

        // When & Then - Aunque el cuerpo esté vacío, el controlador intentará
        // procesarlo
        mockMvc.perform(post("/api/auth/crear-cuenta")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());

        verify(authService, times(1)).crearCuenta(any(Usuario.class));
    }

    @Test
    public void testLogin_WithEmptyRequestBody() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isOk());

        verify(authService, times(1)).login(any(LoginRequest.class));
    }

    @Test
    public void testEndpoints_ContentType() throws Exception {
        // Test para verificar que todos los endpoints retornan JSON
        when(authService.crearCuenta(any(Usuario.class))).thenReturn(tokenResponse);
        when(authService.login(any(LoginRequest.class))).thenReturn(tokenResponse);
        when(authService.refreshToken(anyString())).thenReturn(tokenResponse);

        mockMvc.perform(post("/api/auth/crear-cuenta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/api/auth/refresh-token")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testCrearCuenta_VerifyServiceCall() throws Exception {
        // Given
        when(authService.crearCuenta(any(Usuario.class))).thenReturn(tokenResponse);

        // When
        mockMvc.perform(post("/api/auth/crear-cuenta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)));

        // Then
        verify(authService, times(1)).crearCuenta(argThat(user -> user.getEmail().equals("juan.perez@example.com") &&
                user.getNombre().equals("Juan Pérez")));
    }

    @Test
    public void testLogin_VerifyServiceCall() throws Exception {
        // Given
        when(authService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

        // When
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)));

        // Then
        verify(authService, times(1)).login(argThat(request -> request.email().equals("juan.perez@example.com") &&
                request.password().equals("password123")));
    }

    @Test
    public void testRefreshToken_VerifyServiceCall() throws Exception {
        // Given
        String authHeader = "Bearer valid-refresh-token";
        when(authService.refreshToken(authHeader)).thenReturn(tokenResponse);

        // When
        mockMvc.perform(post("/api/auth/refresh-token")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .contentType(MediaType.APPLICATION_JSON));

        // Then
        verify(authService, times(1)).refreshToken(authHeader);
    }

    @Test
    public void testMultipleCalls() throws Exception {
        // Given
        when(authService.crearCuenta(any(Usuario.class))).thenReturn(tokenResponse);
        when(authService.login(any(LoginRequest.class))).thenReturn(tokenResponse);
        when(authService.refreshToken(anyString())).thenReturn(tokenResponse);

        // When & Then - Realizar múltiples llamadas
        mockMvc.perform(post("/api/auth/crear-cuenta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(usuario)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/refresh-token")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verify que cada servicio fue llamado exactamente una vez
        verify(authService, times(1)).crearCuenta(any(Usuario.class));
        verify(authService, times(1)).login(any(LoginRequest.class));
        verify(authService, times(1)).refreshToken(anyString());
    }
}