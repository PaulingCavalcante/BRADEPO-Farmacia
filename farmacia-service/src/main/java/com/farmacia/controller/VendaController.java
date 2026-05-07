package com.farmacia.controller;

import com.farmacia.service.VendaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class VendaController {

    private final VendaService service;

    public VendaController(VendaService service) {
        this.service = service;
    }

    @PostMapping("/venda")
    public Map<String, Object> vender(@RequestBody Map<String, String> req) {
        return service.processar(req);
    }

    @GetMapping("/notas")
    public List<Map<String, Object>> listar() {
        return service.listarNotas();
    }
}
