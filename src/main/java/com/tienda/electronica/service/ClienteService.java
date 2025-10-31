package com.tienda.electronica.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tienda.electronica.entity.Cliente;
import com.tienda.electronica.exceptions.ClienteNotFoundException;
import com.tienda.electronica.repository.ClienteRepository;

@Service
public class ClienteService {
    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    public Optional<Cliente> obtenerPorId(Long id) {
        return clienteRepository.findById(id);
    }

    public Cliente crear(Cliente cliente) {
        var newCliente = Cliente.builder()
                .nombre(cliente.getNombre())
                .apellidos(cliente.getApellidos())
                .email(cliente.getEmail())
                .telefono(cliente.getTelefono())
                .direccion(cliente.getDireccion())
                .ciudad(cliente.getCiudad())
                .codigoPostal(cliente.getCodigoPostal())
                .activo(true)
                .clientePremium(cliente.isClientePremium())
                .fechaRegistro(LocalDateTime.now())
                .build();
        return clienteRepository.save(newCliente);
    }

    public Cliente actualizar(Long id, Cliente clienteActualizado) {
        return clienteRepository.findById(id)
                .map(cliente -> {
                    cliente.setNombre(clienteActualizado.getNombre());
                    cliente.setApellidos(clienteActualizado.getApellidos());
                    cliente.setEmail(clienteActualizado.getEmail());
                    cliente.setTelefono(clienteActualizado.getTelefono());
                    cliente.setDireccion(clienteActualizado.getDireccion());
                    cliente.setCiudad(clienteActualizado.getCiudad());
                    cliente.setCodigoPostal(clienteActualizado.getCodigoPostal());
                    cliente.setActivo(clienteActualizado.isActivo());
                    cliente.setClientePremium(clienteActualizado.isClientePremium());
                    return clienteRepository.save(cliente);
                })
                .orElseThrow(() -> new ClienteNotFoundException(id));
    }

    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new ClienteNotFoundException(id);
        }
        clienteRepository.deleteById(id);
    }

    public List<Cliente> obtenerActivos() {
        return clienteRepository.findByActivoTrue();
    }

    public List<Cliente> obtenerClientesPremium() {
        return clienteRepository.findByClientePremiumTrue();
    }

    public Optional<Cliente> obtenerPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }

    public List<Cliente> buscarPorNombre(String texto) {
        return clienteRepository.findByNombreOrApellidosContainingIgnoreCase(texto);
    }

    public List<Cliente> obtenerPorCiudad(String ciudad) {
        return clienteRepository.findByCiudadIgnoreCase(ciudad);
    }
}
