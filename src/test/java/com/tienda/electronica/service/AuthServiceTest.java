package com.tienda.electronica.service;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tienda.electronica.entity.Token;
import com.tienda.electronica.entity.Usuario;
import com.tienda.electronica.repository.TokenRepository;
import com.tienda.electronica.repository.UsuarioRepository;
import com.tienda.electronica.request.LoginRequest;
import com.tienda.electronica.response.TokenResponse;

@RunWith(MockitoJUnitRunner.class)
public class AuthServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private TokenRepository tokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private Usuario usuario;
    private LoginRequest loginRequest;
    private String jwtToken;
    private String refreshToken;

    @Before
    public void setUp() {
        // Configurar datos de prueba
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan Pérez")
                .email("juan.perez@example.com")
                .telefono("123456789")
                .password("encodedPassword")
                .activo(true)
                .createdAt(LocalDateTime.now())
                .build();

        loginRequest = new LoginRequest("juan.perez@example.com", "password123");

        jwtToken = "jwt.token.here";
        refreshToken = "refresh.token.here";
    }

    @Test
    public void testCrearCuenta_Success() {
        // Given
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre("María García");
        nuevoUsuario.setEmail("maria.garcia@example.com");
        nuevoUsuario.setTelefono("987654321");
        nuevoUsuario.setPassword("password123");
        nuevoUsuario.setActivo(true);

        Usuario usuarioGuardado = Usuario.builder()
                .id(2L)
                .nombre("María García")
                .email("maria.garcia@example.com")
                .telefono("987654321")
                .password("encodedPassword")
                .activo(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usuarioService.crear(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn(jwtToken);
        when(jwtService.generateRefreshToken(any(Usuario.class))).thenReturn(refreshToken);
        when(tokenRepository.save(any(Token.class))).thenReturn(new Token());

        // When
        TokenResponse response = authService.crearCuenta(nuevoUsuario);

        // Then
        assertNotNull("La respuesta no debe ser nula", response);
        assertEquals("El JWT token debe coincidir", jwtToken, response.accessToken());
        assertEquals("El refresh token debe coincidir", refreshToken, response.refreshToken());

        verify(passwordEncoder, times(1)).encode("password123");
        verify(usuarioService, times(1)).crear(any(Usuario.class));
        verify(jwtService, times(1)).generateToken(any(Usuario.class));
        verify(jwtService, times(1)).generateRefreshToken(any(Usuario.class));
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    public void testLogin_Success() {
        // Given
        when(usuarioService.obtenerPorEmail("juan.perez@example.com"))
                .thenReturn(Optional.of(usuario));
        when(jwtService.generateToken(usuario)).thenReturn(jwtToken);
        when(jwtService.generateRefreshToken(usuario)).thenReturn(refreshToken);
        when(tokenRepository.findAllValidIsFalseOrRevokedIsFalseByUsuarioId(1L))
                .thenReturn(Arrays.asList(new Token(), new Token()));
        when(tokenRepository.saveAll(anyList())).thenReturn(Arrays.asList(new Token(), new Token()));
        when(tokenRepository.save(any(Token.class))).thenReturn(new Token());

        // When
        TokenResponse response = authService.login(loginRequest);

        // Then
        assertNotNull("La respuesta no debe ser nula", response);
        assertEquals("El JWT token debe coincidir", jwtToken, response.accessToken());
        assertEquals("El refresh token debe coincidir", refreshToken, response.refreshToken());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(usuarioService, times(1)).obtenerPorEmail("juan.perez@example.com");
        verify(jwtService, times(1)).generateToken(usuario);
        verify(jwtService, times(1)).generateRefreshToken(usuario);
        verify(tokenRepository, times(1)).findAllValidIsFalseOrRevokedIsFalseByUsuarioId(1L);
        verify(tokenRepository, times(1)).saveAll(anyList());
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test(expected = BadCredentialsException.class)
    public void testLogin_AuthenticationFails() {
        // Given
        doThrow(new BadCredentialsException("Credenciales inválidas"))
                .when(authenticationManager)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        // When - Then (debe lanzar excepción)
        authService.login(loginRequest);
    }

    @Test(expected = RuntimeException.class)
    public void testLogin_UserNotFound() {
        // Given
        when(usuarioService.obtenerPorEmail("juan.perez@example.com"))
                .thenReturn(Optional.empty());

        // When - Then (debe lanzar excepción)
        authService.login(loginRequest);
    }

    @Test
    public void testSaveUserToken() {
        // Given
        when(tokenRepository.save(any(Token.class))).thenReturn(new Token());

        // When
        authService.saveUserToken(usuario, jwtToken);

        // Then
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test
    public void testRefreshToken_Success() {
        // Given
        String authHeader = "Bearer " + refreshToken;
        Usuario usuarioRefresh = Usuario.builder()
                .id(1L)
                .email("juan.perez@example.com")
                .build();

        when(jwtService.extractUsername(refreshToken)).thenReturn("juan.perez@example.com");
        when(usuarioRepository.findByEmail("juan.perez@example.com"))
                .thenReturn(Optional.of(usuarioRefresh));
        when(jwtService.isTokenValid(refreshToken, usuarioRefresh)).thenReturn(true);
        when(jwtService.generateToken(usuarioRefresh)).thenReturn("new.jwt.token");
        when(tokenRepository.findAllValidIsFalseOrRevokedIsFalseByUsuarioId(1L))
                .thenReturn(Arrays.asList());
        when(tokenRepository.save(any(Token.class))).thenReturn(new Token());

        // When
        TokenResponse response = authService.refreshToken(authHeader);

        // Then
        assertNotNull("La respuesta no debe ser nula", response);
        assertEquals("El nuevo JWT token debe coincidir", "new.jwt.token", response.accessToken());
        assertEquals("El refresh token debe mantenerse", refreshToken, response.refreshToken());

        verify(jwtService, times(1)).extractUsername(refreshToken);
        verify(usuarioRepository, times(1)).findByEmail("juan.perez@example.com");
        verify(jwtService, times(1)).isTokenValid(refreshToken, usuarioRefresh);
        verify(jwtService, times(1)).generateToken(usuarioRefresh);
        verify(tokenRepository, times(1)).findAllValidIsFalseOrRevokedIsFalseByUsuarioId(1L);
        verify(tokenRepository, times(1)).save(any(Token.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRefreshToken_InvalidHeader() {
        // Given
        String invalidHeader = "InvalidHeader";

        // When - Then (debe lanzar excepción)
        authService.refreshToken(invalidHeader);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRefreshToken_NullHeader() {
        // When - Then (debe lanzar excepción)
        authService.refreshToken(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRefreshToken_InvalidTokenFormat() {
        // Given
        String invalidHeader = "Bearer";

        // When - Then (debe lanzar excepción)
        authService.refreshToken(invalidHeader);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRefreshToken_UsernameExtractionFails() {
        // Given
        String authHeader = "Bearer " + refreshToken;
        when(jwtService.extractUsername(refreshToken)).thenReturn(null);

        // When - Then (debe lanzar excepción)
        authService.refreshToken(authHeader);
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testRefreshToken_UserNotFound() {
        // Given
        String authHeader = "Bearer " + refreshToken;
        when(jwtService.extractUsername(refreshToken)).thenReturn("noexiste@example.com");
        when(usuarioRepository.findByEmail("noexiste@example.com"))
                .thenReturn(Optional.empty());

        // When - Then (debe lanzar excepción)
        authService.refreshToken(authHeader);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRefreshToken_InvalidToken() {
        // Given
        String authHeader = "Bearer " + refreshToken;
        when(jwtService.extractUsername(refreshToken)).thenReturn("juan.perez@example.com");
        when(usuarioRepository.findByEmail("juan.perez@example.com"))
                .thenReturn(Optional.of(usuario));
        when(jwtService.isTokenValid(refreshToken, usuario)).thenReturn(false);

        // When - Then (debe lanzar excepción)
        authService.refreshToken(authHeader);
    }

    @Test
    public void testCrearCuenta_VerifyUserBuilder() {
        // Given
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre("Test User");
        nuevoUsuario.setEmail("test@example.com");
        nuevoUsuario.setTelefono("123456789");
        nuevoUsuario.setPassword("password123");
        nuevoUsuario.setActivo(true);

        Usuario usuarioGuardado = Usuario.builder()
                .id(1L)
                .nombre("Test User")
                .email("test@example.com")
                .telefono("123456789")
                .password("encodedPassword")
                .activo(true)
                .createdAt(LocalDateTime.now())
                .build();

        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usuarioService.crear(any(Usuario.class))).thenReturn(usuarioGuardado);
        when(jwtService.generateToken(any(Usuario.class))).thenReturn(jwtToken);
        when(jwtService.generateRefreshToken(any(Usuario.class))).thenReturn(refreshToken);
        when(tokenRepository.save(any(Token.class))).thenReturn(new Token());

        // When
        TokenResponse response = authService.crearCuenta(nuevoUsuario);

        // Then
        assertNotNull(response);
        verify(usuarioService).crear(argThat(user -> user.getNombre().equals("Test User") &&
                user.getEmail().equals("test@example.com") &&
                user.getTelefono().equals("123456789") &&
                user.getPassword().equals("encodedPassword") &&
                user.getActivo() &&
                user.getCreatedAt() != null));
    }

    @Test
    public void testSaveUserToken_VerifyTokenBuilder() {
        // Given
        when(tokenRepository.save(any(Token.class))).thenReturn(new Token());

        // When
        authService.saveUserToken(usuario, jwtToken);

        // Then
        verify(tokenRepository).save(argThat(token -> token.getUsuario().equals(usuario) &&
                token.getToken().equals(jwtToken) &&
                token.getType() == Token.TokenType.BEARER &&
                !token.isExpired() &&
                !token.isRevoked()));
    }
}