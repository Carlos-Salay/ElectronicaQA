package com.tienda.electronica.repository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.tienda.electronica.entity.Token;
import com.tienda.electronica.entity.Usuario;

@RunWith(SpringRunner.class)
@DataJpaTest
public class TokenRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario1;
    private Usuario usuario2;
    private Token tokenValido1;
    private Token tokenValido2;
    private Token tokenExpirado;
    private Token tokenRevocado;
    private Token tokenExpiradoYRevocado;

    @Before
    public void setUp() {
        // Limpiar datos existentes
        entityManager.clear();

        // Crear usuarios de prueba
        usuario1 = new Usuario();
        usuario1.setNombre("Juan Pérez");
        usuario1.setEmail("juan.perez@example.com");
        usuario1.setTelefono("123456789");
        usuario1.setActivo(true);
        usuario1.setPassword("password123");

        usuario2 = new Usuario();
        usuario2.setNombre("María García");
        usuario2.setEmail("maria.garcia@example.com");
        usuario2.setTelefono("987654321");
        usuario2.setActivo(true);
        usuario2.setPassword("password456");

        usuario1 = entityManager.persist(usuario1);
        usuario2 = entityManager.persist(usuario2);

        // Crear tokens de prueba
        tokenValido1 = new Token();
        tokenValido1.setToken("token-valido-1");
        tokenValido1.setType(Token.TokenType.BEARER);
        tokenValido1.setExpired(false);
        tokenValido1.setRevoked(false);
        tokenValido1.setUsuario(usuario1);

        tokenValido2 = new Token();
        tokenValido2.setToken("token-valido-2");
        tokenValido2.setType(Token.TokenType.BEARER);
        tokenValido2.setExpired(false);
        tokenValido2.setRevoked(false);
        tokenValido2.setUsuario(usuario1);

        tokenExpirado = new Token();
        tokenExpirado.setToken("token-expirado");
        tokenExpirado.setType(Token.TokenType.BEARER);
        tokenExpirado.setExpired(true);
        tokenExpirado.setRevoked(false);
        tokenExpirado.setUsuario(usuario1);

        tokenRevocado = new Token();
        tokenRevocado.setToken("token-revocado");
        tokenRevocado.setType(Token.TokenType.BEARER);
        tokenRevocado.setExpired(false);
        tokenRevocado.setRevoked(true);
        tokenRevocado.setUsuario(usuario1);

        tokenExpiradoYRevocado = new Token();
        tokenExpiradoYRevocado.setToken("token-expirado-revocado");
        tokenExpiradoYRevocado.setType(Token.TokenType.BEARER);
        tokenExpiradoYRevocado.setExpired(true);
        tokenExpiradoYRevocado.setRevoked(true);
        tokenExpiradoYRevocado.setUsuario(usuario1);

        // Token para otro usuario
        Token tokenOtroUsuario = new Token();
        tokenOtroUsuario.setToken("token-otro-usuario");
        tokenOtroUsuario.setType(Token.TokenType.BEARER);
        tokenOtroUsuario.setExpired(false);
        tokenOtroUsuario.setRevoked(false);
        tokenOtroUsuario.setUsuario(usuario2);

        // Persistir todos los tokens
        entityManager.persist(tokenValido1);
        entityManager.persist(tokenValido2);
        entityManager.persist(tokenExpirado);
        entityManager.persist(tokenRevocado);
        entityManager.persist(tokenExpiradoYRevocado);
        entityManager.persist(tokenOtroUsuario);
        entityManager.flush();
    }

    @Test
    public void testFindAllValidIsFalseOrRevokedIsFalseByUsuarioId_SinTokensValidos() {
        // When
        List<Token> resultado = tokenRepository.findAllValidIsFalseOrRevokedIsFalseByUsuarioId(usuario2.getId());

        // Then
        assertEquals("Debe encontrar 1 token válido para el usuario 2", 1, resultado.size());
        assertTrue("Debe ser un token válido",
                !resultado.get(0).isExpired() && !resultado.get(0).isRevoked());
        assertEquals("Debe ser el token del otro usuario", "token-otro-usuario", resultado.get(0).getToken());
    }

    @Test
    public void testFindAllValidIsFalseOrRevokedIsFalseByUsuarioId_UsuarioSinTokens() {
        // Given - Crear usuario sin tokens
        Usuario usuarioSinTokens = new Usuario();
        usuarioSinTokens.setNombre("Usuario Sin Tokens");
        usuarioSinTokens.setEmail("sin-tokens@example.com");
        usuarioSinTokens.setTelefono("000000000");
        usuarioSinTokens.setActivo(true);
        usuarioSinTokens.setPassword("password789");

        usuarioSinTokens = entityManager.persistAndFlush(usuarioSinTokens);

        // When
        List<Token> resultado = tokenRepository
                .findAllValidIsFalseOrRevokedIsFalseByUsuarioId(usuarioSinTokens.getId());

        // Then
        assertTrue("La lista debe estar vacía para usuario sin tokens", resultado.isEmpty());
    }

    @Test
    public void testFindAllValidIsFalseOrRevokedIsFalseByUsuarioId_UsuarioNoExiste() {
        // When
        List<Token> resultado = tokenRepository.findAllValidIsFalseOrRevokedIsFalseByUsuarioId(999L);

        // Then
        assertTrue("La lista debe estar vacía para usuario que no existe", resultado.isEmpty());
    }

    @Test
    public void testFindByToken_TokenExiste() {
        // When
        Optional<Token> resultado = tokenRepository.findByToken("token-valido-1");

        // Then
        assertTrue("Debe encontrar el token", resultado.isPresent());
        assertEquals("El token debe coincidir", "token-valido-1", resultado.get().getToken());
        assertEquals("El tipo debe ser BEARER", Token.TokenType.BEARER, resultado.get().getType());
        assertFalse("No debe estar expirado", resultado.get().isExpired());
        assertFalse("No debe estar revocado", resultado.get().isRevoked());
        assertEquals("Debe pertenecer al usuario correcto", usuario1.getId(), resultado.get().getUsuario().getId());
    }

    @Test
    public void testFindByToken_TokenNoExiste() {
        // When
        Optional<Token> resultado = tokenRepository.findByToken("token-inexistente");

        // Then
        assertFalse("No debe encontrar token inexistente", resultado.isPresent());
    }

    @Test
    public void testFindByToken_TokenCaseSensitive() {
        // Given
        Token tokenCaseSensitive = new Token();
        tokenCaseSensitive.setToken("Token-Con-Mayusculas");
        tokenCaseSensitive.setType(Token.TokenType.BEARER);
        tokenCaseSensitive.setExpired(false);
        tokenCaseSensitive.setRevoked(false);
        tokenCaseSensitive.setUsuario(usuario1);

        entityManager.persistAndFlush(tokenCaseSensitive);

        // When
        Optional<Token> resultadoExacto = tokenRepository.findByToken("Token-Con-Mayusculas");
        Optional<Token> resultadoMinusculas = tokenRepository.findByToken("token-con-mayusculas");
        Optional<Token> resultadoMayusculas = tokenRepository.findByToken("TOKEN-CON-MAYUSCULAS");

        // Then
        assertTrue("Debe encontrar con case exacto", resultadoExacto.isPresent());
        assertFalse("No debe encontrar con minúsculas (case sensitive)", resultadoMinusculas.isPresent());
        assertFalse("No debe encontrar con mayúsculas (case sensitive)", resultadoMayusculas.isPresent());
    }

    @Test
    public void testFindByToken_TokenConCaracteresEspeciales() {
        // Given
        String tokenEspecial = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        Token tokenJWT = new Token();
        tokenJWT.setToken(tokenEspecial);
        tokenJWT.setType(Token.TokenType.BEARER);
        tokenJWT.setExpired(false);
        tokenJWT.setRevoked(false);
        tokenJWT.setUsuario(usuario1);

        entityManager.persistAndFlush(tokenJWT);

        // When
        Optional<Token> resultado = tokenRepository.findByToken(tokenEspecial);

        // Then
        assertTrue("Debe encontrar token con caracteres especiales", resultado.isPresent());
        assertEquals("El token debe coincidir exactamente", tokenEspecial, resultado.get().getToken());
    }

    @Test
    public void testSaveAndFindById() {
        // Given
        Token nuevoToken = new Token();
        nuevoToken.setToken("nuevo-token");
        nuevoToken.setType(Token.TokenType.BEARER);
        nuevoToken.setExpired(false);
        nuevoToken.setRevoked(false);
        nuevoToken.setUsuario(usuario1);

        // When
        Token guardado = tokenRepository.save(nuevoToken);
        Optional<Token> encontrado = tokenRepository.findById(guardado.getId());

        // Then
        assertTrue("Debe encontrar el token guardado", encontrado.isPresent());
        assertEquals("El token debe coincidir", "nuevo-token", encontrado.get().getToken());
        assertEquals("El tipo debe ser BEARER", Token.TokenType.BEARER, encontrado.get().getType());
        assertFalse("No debe estar expirado", encontrado.get().isExpired());
        assertFalse("No debe estar revocado", encontrado.get().isRevoked());
        assertEquals("Debe pertenecer al usuario correcto", usuario1.getId(), encontrado.get().getUsuario().getId());
    }

    @Test
    public void testDelete() {
        // When
        tokenRepository.deleteById(tokenValido1.getId());
        Optional<Token> resultado = tokenRepository.findById(tokenValido1.getId());

        // Then
        assertFalse("No debe encontrar el token eliminado", resultado.isPresent());
    }

    @Test
    public void testFindAll() {
        // When
        List<Token> resultado = tokenRepository.findAll();

        // Then
        assertEquals("Debe encontrar todos los tokens", 6, resultado.size());
    }

    @Test
    public void testTokenProperties() {
        // When
        Optional<Token> resultado = tokenRepository.findByToken("token-valido-1");

        // Then
        assertTrue("Debe encontrar el token", resultado.isPresent());
        Token token = resultado.get();

        assertNotNull("Debe tener ID", token.getId());
        assertNotNull("Debe tener token string", token.getToken());
        assertNotNull("Debe tener tipo", token.getType());
        assertNotNull("Debe tener estado expired", token.isExpired());
        assertNotNull("Debe tener estado revoked", token.isRevoked());
        assertNotNull("Debe tener usuario", token.getUsuario());
    }

}