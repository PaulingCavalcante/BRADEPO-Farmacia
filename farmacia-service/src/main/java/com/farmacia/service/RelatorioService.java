package com.farmacia.service;

import com.farmacia.dto.ProdutoVendidoResponse;
import com.farmacia.dto.VendaRelatorioResponse;
import com.farmacia.model.Venda;
import com.farmacia.repository.VendaRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Relatórios da Fase 6: vendas por período e produtos mais vendidos. */
@Service
public class RelatorioService {

    private final VendaRepository repository;

    public RelatorioService(VendaRepository repository) {
        this.repository = repository;
    }

    public List<VendaRelatorioResponse> vendasPorPeriodo(LocalDateTime inicio, LocalDateTime fim) {
        return repository.findByDataHoraBetweenOrderByDataHoraAsc(inicio, fim).stream()
                .map(this::toRelatorio)
                .toList();
    }

    public List<ProdutoVendidoResponse> maisVendidos() {
        return repository.rankingMaisVendidos().stream()
                .map(linha -> new ProdutoVendidoResponse(
                        (String) linha[0],
                        (Long) linha[1],
                        (BigDecimal) linha[2]))
                .toList();
    }

    private VendaRelatorioResponse toRelatorio(Venda v) {
        return new VendaRelatorioResponse(v.getId(), v.getProduto(), v.getCpf(),
                v.getCanal() == null ? null : v.getCanal().name(), v.getVendedor(),
                v.getValorLiquido(), v.getComissao(), v.getDataHora());
    }
}
