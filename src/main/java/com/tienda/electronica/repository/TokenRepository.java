package com.tienda.electronica.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tienda.electronica.entity.Token;

public interface TokenRepository extends JpaRepository<Token, Long> {
    List<Token> findAllValidIsFalseOrRevokedIsFalseByUsuarioId(Long id);

    Optional<Token> findByToken(String token);
}
