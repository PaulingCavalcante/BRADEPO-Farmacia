package com.farmacia.repository;

import com.farmacia.dto.VendaResponse;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class VendaRepository {

    private final List<VendaResponse> notas = new ArrayList<>();

    public void salvar(VendaResponse nota) {
        notas.add(nota);
    }

    public List<VendaResponse> listarTodas() {
        return notas;
    }
}
