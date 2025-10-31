package com.tienda.electronica.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tienda.electronica.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByEmail(String email);

    List<Cliente> findByActivoTrue();

    List<Cliente> findByClientePremiumTrue();

    List<Cliente> findByCiudadIgnoreCase(String ciudad);

    List<Cliente> findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase(String nombre, String apellidos);

    default List<Cliente> findByNombreOrApellidosContainingIgnoreCase(String texto) {
        return findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase(texto, texto);
    }

}
