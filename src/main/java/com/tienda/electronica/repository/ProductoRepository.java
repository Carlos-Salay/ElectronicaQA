package com.tienda.electronica.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tienda.electronica.entity.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByCategoriaIgnoreCase(String categoria);

    List<Producto> findByStockGreaterThan(Integer stock);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
