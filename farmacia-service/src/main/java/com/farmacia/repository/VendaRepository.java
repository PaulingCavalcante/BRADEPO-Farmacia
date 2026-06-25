package com.farmacia.repository;

import com.farmacia.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório JPA das vendas. O Spring Data implementa em tempo de execução;
 * não precisa de {@code @Repository}. Substitui o antigo armazenamento em
 * memória ({@code ArrayList}).
 */
public interface VendaRepository extends JpaRepository<Venda, Long> {
}
