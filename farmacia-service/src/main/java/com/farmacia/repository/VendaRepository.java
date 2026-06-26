package com.farmacia.repository;

import com.farmacia.model.Venda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositório JPA das vendas. O Spring Data implementa em tempo de execução;
 * não precisa de {@code @Repository}. Substitui o antigo armazenamento em
 * memória ({@code ArrayList}).
 */
public interface VendaRepository extends JpaRepository<Venda, Long> {

    /** Relatório: vendas dentro de um intervalo de data/hora (Fase 6). */
    List<Venda> findByDataHoraBetweenOrderByDataHoraAsc(LocalDateTime inicio, LocalDateTime fim);

    /**
     * Relatório: ranking de produtos mais vendidos (Fase 6). Retorna linhas
     * {@code [produto, quantidade(Long), totalLiquido(BigDecimal)]} ordenadas
     * pela quantidade desc. O mapeamento para DTO é feito no RelatorioService.
     */
    @Query("SELECT v.produto, COUNT(v), SUM(v.valorLiquido) "
            + "FROM Venda v GROUP BY v.produto ORDER BY COUNT(v) DESC")
    List<Object[]> rankingMaisVendidos();
}
