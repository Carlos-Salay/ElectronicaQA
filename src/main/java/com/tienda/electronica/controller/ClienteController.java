package com.tienda.electronica.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tienda.electronica.entity.Cliente;
import com.tienda.electronica.service.ClienteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "API para gestión de clientes de la tienda electrónica")
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @Operation(summary = "Obtener todos los clientes", description = "Retorna una lista de todos los clientes registrados en la tienda")
    @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente")
    public ResponseEntity<List<Cliente>> obtenerTodos() {
        List<Cliente> clientes = clienteService.obtenerTodos();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cliente por ID", description = "Retorna un cliente específico por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente encontrado"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Cliente> obtenerPorId(
            @Parameter(description = "ID del cliente a buscar") @PathVariable Long id) {
        return clienteService.obtenerPorId(id)
                .map(cliente -> ResponseEntity.ok(cliente))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Registrar nuevo cliente", description = "Registra un nuevo cliente en la tienda electrónica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Cliente registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<Cliente> crear(@RequestBody @Valid Cliente cliente) {
        System.out.println("QUE COSA VIENE ACA 2: " + cliente.toString());
        Cliente clienteCreado = clienteService.crear(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteCreado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cliente actualizado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
    public ResponseEntity<Cliente> actualizar(
            @Parameter(description = "ID del cliente a actualizar") @PathVariable Long id,
            @RequestBody @Valid Cliente cliente) {
        try {
            Cliente clienteActualizado = clienteService.actualizar(id, cliente);
            return ResponseEntity.ok(clienteActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cliente", description = "Elimina un cliente del sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del cliente a eliminar") @PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activos")
    @Operation(summary = "Obtener clientes activos", description = "Retorna solo los clientes que están activos")
    @ApiResponse(responseCode = "200", description = "Lista de clientes activos obtenida exitosamente")
    public ResponseEntity<List<Cliente>> obtenerActivos() {
        List<Cliente> clientes = clienteService.obtenerActivos();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/premium")
    @Operation(summary = "Obtener clientes premium", description = "Retorna solo los clientes con membresía premium")
    @ApiResponse(responseCode = "200", description = "Lista de clientes premium obtenida exitosamente")
    public ResponseEntity<List<Cliente>> obtenerClientesPremium() {
        List<Cliente> clientes = clienteService.obtenerClientesPremium();
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/buscar")
    @Operation(summary = "Buscar clientes por nombre", description = "Busca clientes que contengan el texto especificado en su nombre o apellidos")
    @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente")
    public ResponseEntity<List<Cliente>> buscarPorNombre(
            @Parameter(description = "Texto a buscar en nombre o apellidos") @RequestParam String texto) {
        List<Cliente> clientes = clienteService.buscarPorNombre(texto);
        return ResponseEntity.ok(clientes);
    }

    @GetMapping("/ciudad/{ciudad}")
    @Operation(summary = "Obtener clientes por ciudad", description = "Retorna clientes filtrados por ciudad")
    @ApiResponse(responseCode = "200", description = "Lista de clientes por ciudad obtenida exitosamente")
    public ResponseEntity<List<Cliente>> obtenerPorCiudad(
            @Parameter(description = "Ciudad de los clientes") @PathVariable String ciudad) {
        List<Cliente> clientes = clienteService.obtenerPorCiudad(ciudad);
        return ResponseEntity.ok(clientes);
    }
}
