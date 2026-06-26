package com.farmacia.controller;

import com.farmacia.dto.ProdutoVendidoResponse;
import com.farmacia.dto.VendaRelatorioResponse;
import com.farmacia.service.RelatorioService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Relatórios em /relatorios:
 *  - GET /relatorios/vendas?inicio=2026-06-01&fim=2026-06-30  (datas ISO, inclusive)
 *  - GET /relatorios/mais-vendidos
 */
@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    private final RelatorioService service;

    public RelatorioController(RelatorioService service) {
        this.service = service;
    }

    @GetMapping("/vendas")
    public List<VendaRelatorioResponse> vendasPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        // intervalo inclusivo: do início do dia "inicio" ao fim do dia "fim".
        return service.vendasPorPeriodo(inicio.atStartOfDay(), fim.atTime(LocalTime.MAX));
    }

    @GetMapping("/mais-vendidos")
    public List<ProdutoVendidoResponse> maisVendidos() {
        return service.maisVendidos();
    }
}
