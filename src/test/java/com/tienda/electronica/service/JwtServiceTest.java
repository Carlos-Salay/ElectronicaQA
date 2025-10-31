package com.tienda.electronica.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.MockitoAnnotations.openMocks;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.tienda.electronica.entity.Usuario;

@RunWith(MockitoJUnitRunner.class)
public class JwtServiceTest {

    @InjectMocks
    private JwtService jwtService;

    private Usuario usuario;

    private final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private final long JWT_EXPIRATION = 86400000L; // 24 horas
    private final long REFRESH_EXPIRATION = 604800000L; // 7 días

    @Before
    public void setUp() {
        openMocks(this);

        // Configurar propiedades usando ReflectionTestUtils
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", JWT_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", REFRESH_EXPIRATION);

        // Crear usuario de prueba
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan Pérez");
        usuario.setEmail("juan.perez@example.com");
    }

    @Test
    public void testGenerateToken() {
        // When
        String token = jwtService.generateToken(usuario);

        // Then
        assertNotNull("El token no debe ser nulo", token);
        assertFalse("El token no debe estar vacío", token.isEmpty());
    }

    @Test
    public void testGenerateRefreshToken() {
        // When
        String refreshToken = jwtService.generateRefreshToken(usuario);

        // Then
        assertNotNull("El refresh token no debe ser nulo", refreshToken);
        assertFalse("El refresh token no debe estar vacío", refreshToken.isEmpty());
    }

    @Test
    public void testExtractUsername() {
        // Given
        String token = jwtService.generateToken(usuario);

        // When
        String username = jwtService.extractUsername(token);

        // Then
        assertEquals("El email extraído debe coincidir", usuario.getEmail(), username);
    }

    @Test
    public void testIsTokenValid_WithValidToken() {
        // Given
        String token = jwtService.generateToken(usuario);

        // When
        boolean isValid = jwtService.isTokenValid(token, usuario);

        // Then
        assertTrue("El token debe ser válido", isValid);
    }

    @Test
    public void testIsTokenValid_WithInvalidUser() {
        // Given
        String token = jwtService.generateToken(usuario);
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(2L);
        otroUsuario.setNombre("María García");
        otroUsuario.setEmail("maria.garcia@example.com");

        // When
        boolean isValid = jwtService.isTokenValid(token, otroUsuario);

        // Then
        assertFalse("El token no debe ser válido para otro usuario", isValid);
    }

    @Test
    public void testBuildToken_WithCustomExpiration() {
        // Given
        long customExpiration = 3600000L; // 1 hora

        // When
        String token = jwtService.buildToken(usuario, customExpiration);

        // Then
        assertNotNull("El token no debe ser nulo", token);
        assertFalse("El token no debe estar vacío", token.isEmpty());

        // Verificar que se puede extraer el username
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("El email debe coincidir", usuario.getEmail(), extractedUsername);
    }

    @Test
    public void testTokenContainsUserInformation() {
        // When
        String token = jwtService.generateToken(usuario);
        String extractedUsername = jwtService.extractUsername(token);

        // Then
        assertEquals("El subject del token debe ser el email del usuario",
                usuario.getEmail(), extractedUsername);
    }

    @Test(expected = Exception.class)
    public void testExtractUsername_WithInvalidToken() {
        // Given
        String invalidToken = "token.invalido.malformado";

        // When - Then (debe lanzar excepción)
        jwtService.extractUsername(invalidToken);
    }

    @Test(expected = Exception.class)
    public void testIsTokenValid_WithInvalidToken() {
        // Given
        String invalidToken = "token.invalido.malformado";

        // When - Then (debe lanzar excepción)
        jwtService.isTokenValid(invalidToken, usuario);
    }

    @Test
    public void testDifferentTokensForDifferentUsers() {
        // Given
        Usuario usuario2 = new Usuario();
        usuario2.setId(2L);
        usuario2.setNombre("Ana López");
        usuario2.setEmail("ana.lopez@example.com");

        // When
        String token1 = jwtService.generateToken(usuario);
        String token2 = jwtService.generateToken(usuario2);

        // Then
        assertNotEquals("Los tokens deben ser diferentes para usuarios diferentes",
                token1, token2);

        assertEquals("El token1 debe contener el email del usuario1",
                usuario.getEmail(), jwtService.extractUsername(token1));
        assertEquals("El token2 debe contener el email del usuario2",
                usuario2.getEmail(), jwtService.extractUsername(token2));
    }
}