package com.farmacia.repository;

import com.farmacia.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Repositório JPA dos clientes cadastrados. */
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /** Busca o cliente pelo CPF (usada na venda para aplicar descontos). */
    Optional<Cliente> findByCpf(String cpf);
}
