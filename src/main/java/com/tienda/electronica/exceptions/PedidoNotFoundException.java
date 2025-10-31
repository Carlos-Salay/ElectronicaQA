package com.tienda.electronica.exceptions;

public class PedidoNotFoundException extends RuntimeException {
    public PedidoNotFoundException(Long id) {
        super("Pedido no encontrado con id: " + id);
    }
}
