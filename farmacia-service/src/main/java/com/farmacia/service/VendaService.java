package com.farmacia.service;

import com.farmacia.componentes.ans.AnsClient;
import com.farmacia.componentes.cpf.CpfValidator;
import com.farmacia.componentes.fornecedor.FornecedorAdapter;
import com.farmacia.componentes.sefaz.NotaFiscal;
import com.farmacia.componentes.sefaz.SefazClient;
import com.farmacia.repository.VendaRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class VendaService {

    private static final Set<String> CONTROLADOS = Set.of("rivotril", "diazepam", "ritalina");

    private final CpfValidator cpfValidator;
    private final FornecedorAdapter fornecedor;
    private final SefazClient sefaz;
    private final AnsClient ans;
    private final VendaRepository repository;

    public VendaService(CpfValidator cpfValidator,
                        FornecedorAdapter fornecedor,
                        SefazClient sefaz,
                        AnsClient ans,
                        VendaRepository repository) {
        this.cpfValidator = cpfValidator;
        this.fornecedor = fornecedor;
        this.sefaz = sefaz;
        this.ans = ans;
        this.repository = repository;
    }

    public Map<String, Object> processar(Map<String, String> req) {
        String cpf = req.get("cpf");
        String produto = req.get("produto");

        if (produto.trim().isEmpty()){
            return Map.of("status", "NEGADA", "motivo", "Requisição vazia");
        }

        if (!cpfValidator.validar(cpf)) {
            return Map.of("status", "NEGADA", "motivo", "CPF invalido");
        }

        if (!fornecedor.consultar(produto)) {
            return Map.of("status", "NEGADA", "motivo", "produto sem estoque");
        }

        NotaFiscal nota = new NotaFiscal(UUID.randomUUID().toString(), cpf, produto);
        String protocoloSefaz = sefaz.enviarNota(nota);

        Map<String, Object> resposta = new LinkedHashMap<>();
        resposta.put("status", "AUTORIZADA");
        resposta.put("nota", nota);
        resposta.put("protocoloSefaz", protocoloSefaz);

        if (CONTROLADOS.contains(produto.toLowerCase())) {
            String protocoloAns = ans.enviarReceita(cpf, produto);
            resposta.put("protocoloAns", protocoloAns);
        }

        repository.salvar(resposta);
        return resposta;
    }

    public List<Map<String, Object>> listarNotas() {
        return repository.listarTodas();
    }
}
