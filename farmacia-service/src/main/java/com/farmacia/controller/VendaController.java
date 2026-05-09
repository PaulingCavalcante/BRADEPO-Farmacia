package com.farmacia.controller;

import com.farmacia.dto.VendaRequest;
import com.farmacia.dto.VendaResponse;
import com.farmacia.service.VendaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class VendaController {

    private final VendaService service;

    public VendaController(VendaService service) {
        this.service = service;
    }

    @PostMapping("/venda")
    public VendaResponse vender(@RequestBody VendaRequest req) {
        return service.processar(req);
    }

    @GetMapping("/notas")
    public List<VendaResponse> listar() {
        return service.listarNotas();
    }
}
