package com.tienda.electronica.repository;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.tienda.electronica.entity.Usuario;

@RunWith(SpringRunner.class)
@DataJpaTest
public class UsuarioRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuario1;
    private Usuario usuario2;
    private Usuario usuario3;
    private Usuario usuarioInactivo;

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
        usuario2.setNombre("María García López");
        usuario2.setEmail("maria.garcia@example.com");
        usuario2.setTelefono("987654321");
        usuario2.setActivo(true);
        usuario2.setPassword("password456");

        usuario3 = new Usuario();
        usuario3.setNombre("Carlos Rodríguez");
        usuario3.setEmail("carlos.rodriguez@example.com");
        usuario3.setTelefono("555555555");
        usuario3.setActivo(true);
        usuario3.setPassword("password789");

        usuarioInactivo = new Usuario();
        usuarioInactivo.setNombre("Ana Martínez");
        usuarioInactivo.setEmail("ana.martinez@example.com");
        usuarioInactivo.setTelefono("111111111");
        usuarioInactivo.setActivo(false);
        usuarioInactivo.setPassword("password000");

        // Persistir usuarios
        usuario1 = entityManager.persist(usuario1);
        usuario2 = entityManager.persist(usuario2);
        usuario3 = entityManager.persist(usuario3);
        usuarioInactivo = entityManager.persist(usuarioInactivo);
        entityManager.flush();
    }

    @Test
    public void testFindByEmail_UsuarioExiste() {
        // When
        Optional<Usuario> resultado = usuarioRepository.findByEmail("juan.perez@example.com");

        // Then
        assertTrue("Debe encontrar el usuario por email", resultado.isPresent());
        assertEquals("El email debe coincidir", "juan.perez@example.com", resultado.get().getEmail());
        assertEquals("El nombre debe coincidir", "Juan Pérez", resultado.get().getNombre());
        assertEquals("El teléfono debe coincidir", "123456789", resultado.get().getTelefono());
        assertTrue("Debe estar activo", resultado.get().getActivo());
    }

    @Test
    public void testFindByEmail_UsuarioNoExiste() {
        // When
        Optional<Usuario> resultado = usuarioRepository.findByEmail("noexiste@example.com");

        // Then
        assertFalse("No debe encontrar usuario con email inexistente", resultado.isPresent());
    }

    @Test
    public void testFindByEmail_CaseSensitive() {
        // When
        Optional<Usuario> resultadoExacto = usuarioRepository.findByEmail("juan.perez@example.com");
        Optional<Usuario> resultadoMayusculas = usuarioRepository.findByEmail("JUAN.PEREZ@EXAMPLE.COM");
        Optional<Usuario> resultadoMinusculas = usuarioRepository.findByEmail("juan.perez@example.com");

        // Then
        assertTrue("Debe encontrar con email exacto", resultadoExacto.isPresent());
        assertFalse("No debe encontrar con email en mayúsculas (case sensitive)", resultadoMayusculas.isPresent());
        assertTrue("Debe encontrar con email en minúsculas exacto", resultadoMinusculas.isPresent());
    }

    @Test
    public void testFindByEmail_ConUsuarioInactivo() {
        // When
        Optional<Usuario> resultado = usuarioRepository.findByEmail("ana.martinez@example.com");

        // Then
        assertTrue("Debe encontrar usuario inactivo por email", resultado.isPresent());
        assertEquals("El email debe coincidir", "ana.martinez@example.com", resultado.get().getEmail());
        assertFalse("Debe estar inactivo", resultado.get().getActivo());
    }

    @Test
    public void testFindByNombreContainingIgnoreCase_EncuentraResultados() {
        // When
        Optional<Usuario> resultadoJuan = usuarioRepository.findByNombreContainingIgnoreCase("juan");
        Optional<Usuario> resultadoJUAN = usuarioRepository.findByNombreContainingIgnoreCase("JUAN");
        Optional<Usuario> resultadoMaria = usuarioRepository.findByNombreContainingIgnoreCase("maría");
        Optional<Usuario> resultadoGarcia = usuarioRepository.findByNombreContainingIgnoreCase("garcía");
        Optional<Usuario> resultadoCarlos = usuarioRepository.findByNombreContainingIgnoreCase("carlos");

        // Then
        assertTrue("Debe encontrar usuario con 'juan' en el nombre (case insensitive)", resultadoJuan.isPresent());
        assertTrue("Debe encontrar usuario con 'JUAN' en el nombre (case insensitive)", resultadoJUAN.isPresent());
        assertTrue("Debe encontrar usuario con 'maría' en el nombre", resultadoMaria.isPresent());
        assertTrue("Debe encontrar usuario con 'garcía' en el nombre", resultadoGarcia.isPresent());
        assertTrue("Debe encontrar usuario con 'carlos' en el nombre", resultadoCarlos.isPresent());

        assertEquals("El usuario con 'juan' debe ser Juan Pérez", "Juan Pérez", resultadoJuan.get().getNombre());
        assertEquals("El usuario con 'garcía' debe ser María García", "María García López",
                resultadoGarcia.get().getNombre());
    }

    @Test
    public void testFindByNombreContainingIgnoreCase_TextoParcial() {
        // When
        Optional<Usuario> resultadoPerez = usuarioRepository.findByNombreContainingIgnoreCase("pérez");
        Optional<Usuario> resultadoRodriguez = usuarioRepository.findByNombreContainingIgnoreCase("rodríguez");
        Optional<Usuario> resultadoLopez = usuarioRepository.findByNombreContainingIgnoreCase("lópez");

        // Then
        assertTrue("Debe encontrar usuario con 'pérez' en el nombre", resultadoPerez.isPresent());
        assertTrue("Debe encontrar usuario con 'rodríguez' en el nombre", resultadoRodriguez.isPresent());
        assertTrue("Debe encontrar usuario con 'lópez' en el nombre", resultadoLopez.isPresent());

        assertEquals("El usuario con 'pérez' debe ser Juan Pérez", "Juan Pérez", resultadoPerez.get().getNombre());
        assertEquals("El usuario con 'lópez' debe ser María García López", "María García López",
                resultadoLopez.get().getNombre());
    }

    @Test
    public void testFindByNombreContainingIgnoreCase_TextoInexistente() {
        // When
        Optional<Usuario> resultado = usuarioRepository.findByNombreContainingIgnoreCase("inexistente");

        // Then
        assertFalse("No debe encontrar usuario con texto inexistente", resultado.isPresent());
    }

    @Test
    public void testFindByNombreContainingIgnoreCase_ConUsuarioInactivo() {
        // When
        Optional<Usuario> resultado = usuarioRepository.findByNombreContainingIgnoreCase("ana");

        // Then
        assertTrue("Debe encontrar usuario inactivo por nombre", resultado.isPresent());
        assertEquals("El nombre debe coincidir", "Ana Martínez", resultado.get().getNombre());
        assertFalse("Debe estar inactivo", resultado.get().getActivo());
    }

    @Test
    public void testFindByActivoTrue() {
        // When
        List<Usuario> resultado = usuarioRepository.findByActivoTrue();

        // Then
        assertEquals("Debe encontrar 3 usuarios activos", 3, resultado.size());
        assertTrue("Todos deben estar activos",
                resultado.stream().allMatch(Usuario::getActivo));
        assertTrue("Debe contener a Juan Pérez",
                resultado.stream().anyMatch(u -> "Juan Pérez".equals(u.getNombre())));
        assertTrue("Debe contener a María García López",
                resultado.stream().anyMatch(u -> "María García López".equals(u.getNombre())));
        assertTrue("Debe contener a Carlos Rodríguez",
                resultado.stream().anyMatch(u -> "Carlos Rodríguez".equals(u.getNombre())));
        assertFalse("No debe contener a Ana Martínez (inactiva)",
                resultado.stream().anyMatch(u -> "Ana Martínez".equals(u.getNombre())));
    }

    @Test
    public void testSaveAndFindById() {
        // Given
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre("Nuevo Usuario");
        nuevoUsuario.setEmail("nuevo@example.com");
        nuevoUsuario.setTelefono("999999999");
        nuevoUsuario.setActivo(true);
        nuevoUsuario.setPassword("nuevopassword");

        // When
        Usuario guardado = usuarioRepository.save(nuevoUsuario);
        Optional<Usuario> encontrado = usuarioRepository.findById(guardado.getId());

        // Then
        assertTrue("Debe encontrar el usuario guardado", encontrado.isPresent());
        assertEquals("El nombre debe coincidir", "Nuevo Usuario", encontrado.get().getNombre());
        assertEquals("El email debe coincidir", "nuevo@example.com", encontrado.get().getEmail());
        assertEquals("El teléfono debe coincidir", "999999999", encontrado.get().getTelefono());
        assertTrue("Debe estar activo", encontrado.get().getActivo());
    }

    @Test
    public void testDelete() {
        // When
        usuarioRepository.deleteById(usuario1.getId());
        Optional<Usuario> resultado = usuarioRepository.findById(usuario1.getId());

        // Then
        assertFalse("No debe encontrar el usuario eliminado", resultado.isPresent());
    }

    @Test
    public void testFindAll() {
        // When
        List<Usuario> resultado = usuarioRepository.findAll();

        // Then
        assertEquals("Debe encontrar todos los usuarios (activos e inactivos)", 4, resultado.size());
    }

    @Test
    public void testUpdateUsuario() {
        // Given
        Usuario usuario = usuarioRepository.findById(usuario1.getId()).get();
        usuario.setNombre("Juan Pérez Actualizado");
        usuario.setEmail("juan.actualizado@example.com");
        usuario.setActivo(false);

        // When
        Usuario actualizado = usuarioRepository.save(usuario);
        Optional<Usuario> resultado = usuarioRepository.findById(usuario1.getId());

        // Then
        assertTrue("Debe encontrar el usuario actualizado", resultado.isPresent());
        assertEquals("El nombre debe estar actualizado", "Juan Pérez Actualizado", resultado.get().getNombre());
        assertEquals("El email debe estar actualizado", "juan.actualizado@example.com", resultado.get().getEmail());
        assertFalse("Debe estar inactivo", resultado.get().getActivo());
    }

    @Test
    public void testFindByEmail_AfterUpdate() {
        // Given
        Usuario usuario = usuarioRepository.findById(usuario1.getId()).get();
        usuario.setEmail("nuevo.email@example.com");
        usuarioRepository.save(usuario);

        // When
        Optional<Usuario> resultadoViejo = usuarioRepository.findByEmail("juan.perez@example.com");
        Optional<Usuario> resultadoNuevo = usuarioRepository.findByEmail("nuevo.email@example.com");

        // Then
        assertFalse("No debe encontrar con el email viejo", resultadoViejo.isPresent());
        assertTrue("Debe encontrar con el email nuevo", resultadoNuevo.isPresent());
        assertEquals("El nombre debe coincidir", "Juan Pérez", resultadoNuevo.get().getNombre());
    }

    @Test
    public void testUsuarioProperties() {
        // When
        Optional<Usuario> resultado = usuarioRepository.findByEmail("juan.perez@example.com");

        // Then
        assertTrue("Debe encontrar el usuario", resultado.isPresent());
        Usuario usuario = resultado.get();

        assertNotNull("Debe tener ID", usuario.getId());
        assertNotNull("Debe tener nombre", usuario.getNombre());
        assertNotNull("Debe tener email", usuario.getEmail());
        assertNotNull("Debe tener teléfono", usuario.getTelefono());
        assertNotNull("Debe tener estado activo", usuario.getActivo());
        assertNotNull("Debe tener password", usuario.getPassword());
    }
}