package com.tienda.electronica.repository;

import static org.junit.Assert.*;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import com.tienda.electronica.entity.Cliente;

@RunWith(SpringRunner.class)
@DataJpaTest
public class ClienteRepositoryTest {

        @Autowired
        private TestEntityManager entityManager;

        @Autowired
        private ClienteRepository clienteRepository;

        private Cliente crearCliente(Long id, String nombre, String apellidos, String email,
                        String ciudad, boolean activo, boolean premium) {
                Cliente cliente = new Cliente();
                if (id != null) {
                        cliente.setId(id);
                }
                cliente.setNombre(nombre);
                cliente.setApellidos(apellidos);
                cliente.setEmail(email);
                cliente.setTelefono("123456789");
                cliente.setDireccion("Dirección " + nombre);
                cliente.setCiudad(ciudad);
                cliente.setActivo(activo);
                cliente.setClientePremium(premium);
                cliente.setCodigoPostal(ciudad);
                return cliente;
        }

        @Test
        public void testFindByEmail_ClienteExiste() {
                // Given
                Cliente cliente = crearCliente(null, "Juan", "Pérez García", "juan.perez@example.com",
                                "Madrid", true, false);
                entityManager.persistAndFlush(cliente);

                // When
                Optional<Cliente> resultado = clienteRepository.findByEmail("juan.perez@example.com");

                // Then
                assertTrue("Debe encontrar el cliente por email", resultado.isPresent());
                assertEquals("El email debe coincidir", "juan.perez@example.com", resultado.get().getEmail());
                assertEquals("El nombre debe coincidir", "Juan", resultado.get().getNombre());
        }

        @Test
        public void testFindByEmail_ClienteNoExiste() {
                // When
                Optional<Cliente> resultado = clienteRepository.findByEmail("noexiste@example.com");

                // Then
                assertFalse("No debe encontrar el cliente", resultado.isPresent());
        }

        @Test
        public void testFindByEmail_CaseSensitive() {
                // Given
                Cliente cliente = crearCliente(null, "María", "López Martínez", "maria.lopez@example.com",
                                "Barcelona", true, true);
                entityManager.persistAndFlush(cliente);

                // When
                Optional<Cliente> resultado1 = clienteRepository.findByEmail("MARIA.LOPEZ@EXAMPLE.COM");
                Optional<Cliente> resultado2 = clienteRepository.findByEmail("maria.lopez@example.com");

                // Then
                assertFalse("No debe encontrar con email en mayúsculas", resultado1.isPresent());
                assertTrue("Debe encontrar con email exacto", resultado2.isPresent());
        }

        @Test
        public void testFindByActivoTrue_SinClientesActivos() {
                // Given
                Cliente clienteInactivo1 = crearCliente(null, "Cliente1", "Apellido1", "cliente1@example.com",
                                "Madrid", false, false);
                Cliente clienteInactivo2 = crearCliente(null, "Cliente2", "Apellido2", "cliente2@example.com",
                                "Barcelona", false, true);

                entityManager.persist(clienteInactivo1);
                entityManager.persist(clienteInactivo2);
                entityManager.flush();

                // When
                List<Cliente> resultado = clienteRepository.findByActivoTrue();

                // Then
                assertTrue("La lista debe estar vacía", resultado.isEmpty());
        }

        @Test
        public void testFindByCiudadIgnoreCase() {
                // Given
                Cliente clienteMadrid1 = crearCliente(null, "Cliente1", "Apellido1", "cliente1@example.com",
                                "Madrid", true, false);
                Cliente clienteMadrid2 = crearCliente(null, "Cliente2", "Apellido2", "cliente2@example.com",
                                "Madrid", true, true);
                Cliente clienteBarcelona = crearCliente(null, "Cliente3", "Apellido3", "cliente3@example.com",
                                "Barcelona", true, false);

                entityManager.persist(clienteMadrid1);
                entityManager.persist(clienteMadrid2);
                entityManager.persist(clienteBarcelona);
                entityManager.flush();

                // When
                List<Cliente> resultado1 = clienteRepository.findByCiudadIgnoreCase("madrid");
                List<Cliente> resultado2 = clienteRepository.findByCiudadIgnoreCase("MADRID");
                List<Cliente> resultado3 = clienteRepository.findByCiudadIgnoreCase("Madrid");

                // Then
                assertEquals("Debe encontrar 2 clientes en Madrid (case insensitive)", 2, resultado1.size());
                assertEquals("Debe encontrar 2 clientes en MADRID (case insensitive)", 2, resultado2.size());
                assertEquals("Debe encontrar 2 clientes en Madrid (case insensitive)", 2, resultado3.size());
                assertTrue("Todos deben ser de Madrid",
                                resultado1.stream().allMatch(c -> "Madrid".equals(c.getCiudad())));
        }

        @Test
        public void testFindByCiudadIgnoreCase_CiudadNoExiste() {
                // Given
                Cliente cliente = crearCliente(null, "Cliente1", "Apellido1", "cliente1@example.com",
                                "Madrid", true, false);
                entityManager.persistAndFlush(cliente);

                // When
                List<Cliente> resultado = clienteRepository.findByCiudadIgnoreCase("CiudadInexistente");

                // Then
                assertTrue("La lista debe estar vacía", resultado.isEmpty());
        }

        @Test
        public void testFindByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase_PorNombre() {
                // Given
                Cliente cliente1 = crearCliente(null, "Juan", "Pérez García", "juan@example.com",
                                "Madrid", true, false);
                Cliente cliente2 = crearCliente(null, "Juana", "López Martínez", "juana@example.com",
                                "Barcelona", true, true);
                Cliente cliente3 = crearCliente(null, "Carlos", "González Ruiz", "carlos@example.com",
                                "Valencia", true, false);

                entityManager.persist(cliente1);
                entityManager.persist(cliente2);
                entityManager.persist(cliente3);
                entityManager.flush();

                // When
                List<Cliente> resultado = clienteRepository
                                .findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase("juan", "juan");

                // Then
                assertEquals("Debe encontrar 2 clientes con 'juan' en nombre o apellidos", 2, resultado.size());
                assertTrue("Debe contener a Juan Pérez",
                                resultado.stream().anyMatch(c -> "Juan".equals(c.getNombre())));
                assertTrue("Debe contener a Juana López",
                                resultado.stream().anyMatch(c -> "Juana".equals(c.getNombre())));
        }

        @Test
        public void testFindByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase_PorApellidos() {
                // Given
                Cliente cliente1 = crearCliente(null, "Ana", "García Pérez", "ana@example.com",
                                "Madrid", true, false);
                Cliente cliente2 = crearCliente(null, "Luis", "Pérez López", "luis@example.com",
                                "Barcelona", true, true);
                Cliente cliente3 = crearCliente(null, "Marta", "Martínez Ruiz", "marta@example.com",
                                "Valencia", true, false);

                entityManager.persist(cliente1);
                entityManager.persist(cliente2);
                entityManager.persist(cliente3);
                entityManager.flush();

                // When
                List<Cliente> resultado = clienteRepository
                                .findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase("pérez", "pérez");

                // Then
                assertEquals("Debe encontrar 2 clientes con 'pérez' en nombre o apellidos", 2, resultado.size());
                assertTrue("Debe contener a Ana García Pérez",
                                resultado.stream().anyMatch(c -> "García Pérez".equals(c.getApellidos())));
                assertTrue("Debe contener a Luis Pérez López",
                                resultado.stream().anyMatch(c -> "Pérez López".equals(c.getApellidos())));
        }

        @Test
        public void testFindByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase_CaseInsensitive() {
                // Given
                Cliente cliente = crearCliente(null, "MARÍA", "GARCÍA", "maria@example.com",
                                "Madrid", true, false);
                entityManager.persistAndFlush(cliente);

                // When
                List<Cliente> resultado1 = clienteRepository
                                .findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase("maría", "maría");
                List<Cliente> resultado2 = clienteRepository
                                .findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase("MARÍA", "MARÍA");
                List<Cliente> resultado3 = clienteRepository
                                .findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase("María", "María");

                // Then
                assertEquals("Debe encontrar 1 cliente (case insensitive)", 1, resultado1.size());
                assertEquals("Debe encontrar 1 cliente (case insensitive)", 1, resultado2.size());
                assertEquals("Debe encontrar 1 cliente (case insensitive)", 1, resultado3.size());
        }

        @Test
        public void testFindByNombreOrApellidosContainingIgnoreCase() {
                // Given
                Cliente cliente1 = crearCliente(null, "Juan", "Pérez García", "juan@example.com",
                                "Madrid", true, false);
                Cliente cliente2 = crearCliente(null, "Ana", "García López", "ana@example.com",
                                "Barcelona", true, true);
                Cliente cliente3 = crearCliente(null, "Luis", "Martínez Ruiz", "luis@example.com",
                                "Valencia", true, false);

                entityManager.persist(cliente1);
                entityManager.persist(cliente2);
                entityManager.persist(cliente3);
                entityManager.flush();

                // When
                List<Cliente> resultado = clienteRepository.findByNombreOrApellidosContainingIgnoreCase("garcía");

                // Then
                assertEquals("Debe encontrar 2 clientes con 'garcía' en nombre o apellidos", 2, resultado.size());
                assertTrue("Debe contener a Juan Pérez García",
                                resultado.stream().anyMatch(c -> "Pérez García".equals(c.getApellidos())));
                assertTrue("Debe contener a Ana García López",
                                resultado.stream().anyMatch(c -> "García López".equals(c.getApellidos())));
        }

        @Test
        public void testFindByNombreOrApellidosContainingIgnoreCase_TextoVacio() {
                // Given
                Cliente cliente1 = crearCliente(null, "Juan", "Pérez García", "juan@example.com",
                                "Madrid", true, false);
                Cliente cliente2 = crearCliente(null, "Ana", "García López", "ana@example.com",
                                "Barcelona", true, true);

                entityManager.persist(cliente1);
                entityManager.persist(cliente2);
                entityManager.flush();

                // When
                List<Cliente> resultado = clienteRepository.findByNombreOrApellidosContainingIgnoreCase("");

                // Then
                assertEquals("Debe encontrar todos los clientes con texto vacío", 2, resultado.size());
        }

        @Test
        public void testFindByNombreOrApellidosContainingIgnoreCase_TextoCorto() {
                // Given
                Cliente cliente1 = crearCliente(null, "Ana", "López García", "ana@example.com",
                                "Madrid", true, false);
                entityManager.persistAndFlush(cliente1);

                // When
                List<Cliente> resultado = clienteRepository.findByNombreOrApellidosContainingIgnoreCase("a");

                // Then
                assertEquals("Debe encontrar clientes con 'a' en nombre o apellidos", 1, resultado.size());
        }

        @Test
        public void testSaveAndFindById() {
                // Given
                Cliente cliente = crearCliente(null, "Nuevo", "Cliente", "nuevo@example.com",
                                "Sevilla", true, false);

                // When
                Cliente guardado = clienteRepository.save(cliente);
                Optional<Cliente> encontrado = clienteRepository.findById(guardado.getId());

                // Then
                assertTrue("Debe encontrar el cliente guardado", encontrado.isPresent());
                assertEquals("El nombre debe coincidir", "Nuevo", encontrado.get().getNombre());
                assertEquals("El email debe coincidir", "nuevo@example.com", encontrado.get().getEmail());
        }

        @Test
        public void testDelete() {
                // Given
                Cliente cliente = crearCliente(null, "ParaEliminar", "Cliente", "eliminar@example.com",
                                "Madrid", true, false);
                Cliente guardado = entityManager.persistAndFlush(cliente);

                // When
                clienteRepository.deleteById(guardado.getId());
                Optional<Cliente> resultado = clienteRepository.findById(guardado.getId());

                // Then
                assertFalse("No debe encontrar el cliente eliminado", resultado.isPresent());
        }

        @Test
        public void testFindAll() {
                // Given
                Cliente cliente1 = crearCliente(null, "Cliente1", "Apellido1", "cliente1@example.com",
                                "Madrid", true, false);
                Cliente cliente2 = crearCliente(null, "Cliente2", "Apellido2", "cliente2@example.com",
                                "Barcelona", true, true);
                Cliente cliente3 = crearCliente(null, "Cliente3", "Apellido3", "cliente3@example.com",
                                "Valencia", false, false);

                entityManager.persist(cliente1);
                entityManager.persist(cliente2);
                entityManager.persist(cliente3);
                entityManager.flush();

                // When
                List<Cliente> resultado = clienteRepository.findAll();

                // Then
                assertEquals("Debe encontrar todos los clientes", 3, resultado.size());
        }
}