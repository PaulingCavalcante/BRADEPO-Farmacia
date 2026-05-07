package com.farmacia.repository;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class VendaRepository {

    private final List<Map<String, Object>> notas = new ArrayList<>();

    public void salvar(Map<String, Object> nota) {
        notas.add(nota);
    }

    public List<Map<String, Object>> listarTodas() {
        return notas;
    }
}
