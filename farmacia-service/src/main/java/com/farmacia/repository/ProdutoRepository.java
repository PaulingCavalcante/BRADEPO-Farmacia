package com.farmacia.repository;

import com.farmacia.model.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositório JPA dos produtos cadastrados. Implementado pelo Spring Data em
 * tempo de execução.
 */
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    /** Busca por nome ignorando maiúsculas/minúsculas (usada na venda). */
    Optional<Produto> findByNomeIgnoreCase(String nome);
}
