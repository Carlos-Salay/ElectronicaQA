package com.tienda.electronica.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.tienda.electronica.entity.Usuario;
import com.tienda.electronica.exceptions.UsuarioNotFoundException;
import com.tienda.electronica.repository.UsuarioRepository;

@RunWith(MockitoJUnitRunner.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario1;
    private Usuario usuario2;
    private Usuario usuarioInactivo;

    @Before
    public void setUp() {
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

        usuarioInactivo = new Usuario();
        usuarioInactivo.setId(3L);
        usuarioInactivo.setNombre("Carlos López");
        usuarioInactivo.setEmail("carlos.lopez@example.com");
        usuarioInactivo.setTelefono("555555555");
        usuarioInactivo.setActivo(false);
    }

    @Test
    public void testObtenerTodos() {
        // Given
        List<Usuario> usuarios = Arrays.asList(usuario1, usuario2);
        when(usuarioRepository.findAll()).thenReturn(usuarios);

        // When
        List<Usuario> resultado = usuarioService.obtenerTodos();

        // Then
        assertNotNull("La lista no debe ser nula", resultado);
        assertEquals("Debe retornar 2 usuarios", 2, resultado.size());
        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    public void testObtenerPorId_UsuarioExiste() {
        // Given
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));

        // When
        Optional<Usuario> resultado = usuarioService.obtenerPorId(1L);

        // Then
        assertTrue("Debe encontrar el usuario", resultado.isPresent());
        assertEquals("El ID debe coincidir", usuario1.getId(), resultado.get().getId());
        assertEquals("El nombre debe coincidir", usuario1.getNombre(), resultado.get().getNombre());
        verify(usuarioRepository, times(1)).findById(1L);
    }

    @Test
    public void testObtenerPorId_UsuarioNoExiste() {
        // Given
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // When
        Optional<Usuario> resultado = usuarioService.obtenerPorId(99L);

        // Then
        assertFalse("No debe encontrar el usuario", resultado.isPresent());
        verify(usuarioRepository, times(1)).findById(99L);
    }

    @Test
    public void testCrear_UsuarioValido() {
        // Given
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre("Ana Torres");
        nuevoUsuario.setEmail("ana.torres@example.com");
        nuevoUsuario.setTelefono("111111111");
        nuevoUsuario.setActivo(true);

        when(usuarioRepository.findByEmail("ana.torres@example.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(nuevoUsuario);

        // When
        Usuario resultado = usuarioService.crear(nuevoUsuario);

        // Then
        assertNotNull("El usuario creado no debe ser nulo", resultado);
        assertEquals("El email debe coincidir", "ana.torres@example.com", resultado.getEmail());
        verify(usuarioRepository, times(1)).findByEmail("ana.torres@example.com");
        verify(usuarioRepository, times(1)).save(nuevoUsuario);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCrear_EmailYaExiste() {
        // Given
        when(usuarioRepository.findByEmail("juan.perez@example.com")).thenReturn(Optional.of(usuario1));

        // When - Then (debe lanzar excepción)
        usuarioService.crear(usuario1);
    }

    @Test
    public void testActualizar_UsuarioExiste() {
        // Given
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setNombre("Juan Pérez Actualizado");
        usuarioActualizado.setEmail("juan.actualizado@example.com");
        usuarioActualizado.setTelefono("999999999");
        usuarioActualizado.setActivo(false);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioActualizado);

        // When
        Usuario resultado = usuarioService.actualizar(1L, usuarioActualizado);

        // Then
        assertNotNull("El usuario actualizado no debe ser nulo", resultado);
        assertEquals("El nombre debe estar actualizado", "Juan Pérez Actualizado", resultado.getNombre());
        assertEquals("El email debe estar actualizado", "juan.actualizado@example.com", resultado.getEmail());
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test(expected = UsuarioNotFoundException.class)
    public void testActualizar_UsuarioNoExiste() {
        // Given
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        // When - Then (debe lanzar excepción)
        usuarioService.actualizar(99L, usuario1);
    }

    @Test
    public void testEliminar() {
        // Given - no necesitamos configurar when() porque deleteById es void

        // When
        usuarioService.eliminar(1L);

        // Then
        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testObtenerActivos() {
        // Given
        List<Usuario> usuariosActivos = Arrays.asList(usuario1, usuario2);
        when(usuarioRepository.findByActivoTrue()).thenReturn(usuariosActivos);

        // When
        List<Usuario> resultado = usuarioService.obtenerActivos();

        // Then
        assertNotNull("La lista no debe ser nula", resultado);
        assertEquals("Debe retornar 2 usuarios activos", 2, resultado.size());
        assertTrue("Todos deben estar activos",
                resultado.stream().allMatch(Usuario::getActivo));
        verify(usuarioRepository, times(1)).findByActivoTrue();
    }

    @Test
    public void testObtenerPorEmail_UsuarioExiste() {
        // Given
        when(usuarioRepository.findByEmail("juan.perez@example.com")).thenReturn(Optional.of(usuario1));

        // When
        Optional<Usuario> resultado = usuarioService.obtenerPorEmail("juan.perez@example.com");

        // Then
        assertTrue("Debe encontrar el usuario por email", resultado.isPresent());
        assertEquals("El email debe coincidir", "juan.perez@example.com", resultado.get().getEmail());
        verify(usuarioRepository, times(1)).findByEmail("juan.perez@example.com");
    }

    @Test
    public void testObtenerPorEmail_UsuarioNoExiste() {
        // Given
        when(usuarioRepository.findByEmail("noexiste@example.com")).thenReturn(Optional.empty());

        // When
        Optional<Usuario> resultado = usuarioService.obtenerPorEmail("noexiste@example.com");

        // Then
        assertFalse("No debe encontrar el usuario", resultado.isPresent());
        verify(usuarioRepository, times(1)).findByEmail("noexiste@example.com");
    }

    @Test
    public void testBuscarPorNombre_NoEncuentraResultados() {
        // Given
        when(usuarioRepository.findByNombreContainingIgnoreCase("inexistente")).thenReturn(Optional.empty());

        // When
        Optional<Usuario> resultado = usuarioService.buscarPorNombre("inexistente");

        // Then
        assertFalse("No debe encontrar usuarios", resultado.isPresent());
        verify(usuarioRepository, times(1)).findByNombreContainingIgnoreCase("inexistente");
    }

    @Test
    public void testActualizar_CamposCompletos() {
        // Given
        Usuario usuarioActualizado = new Usuario();
        usuarioActualizado.setNombre("Nuevo Nombre");
        usuarioActualizado.setEmail("nuevo.email@example.com");
        usuarioActualizado.setTelefono("000000000");
        usuarioActualizado.setActivo(false);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario1));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Usuario resultado = usuarioService.actualizar(1L, usuarioActualizado);

        // Then
        assertEquals("El nombre debe actualizarse", "Nuevo Nombre", resultado.getNombre());
        assertEquals("El email debe actualizarse", "nuevo.email@example.com", resultado.getEmail());
        assertEquals("El teléfono debe actualizarse", "000000000", resultado.getTelefono());
        assertFalse("El estado debe actualizarse", resultado.getActivo());
    }

    @Test
    public void testCrear_VerificarLlamadasRepository() {
        // Given
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmail("nuevo@example.com");

        when(usuarioRepository.findByEmail("nuevo@example.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(nuevoUsuario)).thenReturn(nuevoUsuario);

        // When
        usuarioService.crear(nuevoUsuario);

        // Then
        verify(usuarioRepository, times(1)).findByEmail("nuevo@example.com");
        verify(usuarioRepository, times(1)).save(nuevoUsuario);
        verifyNoMoreInteractions(usuarioRepository);
    }
}