package com.farmacia.service;

import com.farmacia.componentes.ans.AnsClient;
import com.farmacia.componentes.cpf.CpfValidator;
import com.farmacia.componentes.fornecedor.FornecedorAdapter;
import com.farmacia.componentes.sefaz.NotaFiscal;
import com.farmacia.componentes.sefaz.SefazClient;
import com.farmacia.dto.VendaRequest;
import com.farmacia.dto.VendaResponse;
import com.farmacia.repository.VendaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public VendaResponse processar(VendaRequest req) {
        String cpf = req.cpf();
        String produto = req.produto();

        if (produto == null || produto.trim().isEmpty()) {
            return new VendaResponse("NEGADA", null, null, null, "Requisição vazia");
        }

        if (!cpfValidator.validar(cpf)) {
            return new VendaResponse("NEGADA", null, null, null, "CPF invalido");
        }

        if (!fornecedor.consultar(produto)) {
            return new VendaResponse("NEGADA", null, null, null, "produto sem estoque");
        }

        NotaFiscal nota = new NotaFiscal(UUID.randomUUID().toString(), cpf, produto);
        String protocoloSefaz = sefaz.enviarNota(nota);

        String protocoloAns = null;
        if (CONTROLADOS.contains(produto.toLowerCase())) {
            protocoloAns = ans.enviarReceita(cpf, produto);
        }

        VendaResponse resposta = new VendaResponse("AUTORIZADA", nota, protocoloSefaz, protocoloAns, null);
        repository.salvar(resposta);
        return resposta;
    }

    public List<VendaResponse> listarNotas() {
        return repository.listarTodas();
    }
}
