package com.farmacia.service;

import com.farmacia.componentes.ans.AnsClient;
import com.farmacia.componentes.cpf.CpfValidator;
import com.farmacia.componentes.fornecedor.FornecedorAdapter;
import com.farmacia.componentes.sefaz.NotaFiscal;
import com.farmacia.componentes.sefaz.SefazClient;
import com.farmacia.dto.VendaRequest;
import com.farmacia.dto.VendaResponse;
import com.farmacia.model.Produto;
import com.farmacia.model.Venda;
import com.farmacia.repository.ProdutoRepository;
import com.farmacia.repository.VendaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class VendaService {

    private final CpfValidator cpfValidator;
    private final FornecedorAdapter fornecedor;
    private final SefazClient sefaz;
    private final AnsClient ans;
    private final VendaRepository repository;
    private final ProdutoRepository produtoRepository;

    public VendaService(CpfValidator cpfValidator,
                        FornecedorAdapter fornecedor,
                        SefazClient sefaz,
                        AnsClient ans,
                        VendaRepository repository,
                        ProdutoRepository produtoRepository) {
        this.cpfValidator = cpfValidator;
        this.fornecedor = fornecedor;
        this.sefaz = sefaz;
        this.ans = ans;
        this.repository = repository;
        this.produtoRepository = produtoRepository;
    }

    public VendaResponse processar(VendaRequest req) {
        String cpf = req.cpf();
        String nomeProduto = req.produto();

        if (nomeProduto == null || nomeProduto.trim().isEmpty()) {
            return new VendaResponse("NEGADA", null, null, null, "Requisição vazia");
        }

        // O produto precisa estar cadastrado (fonte da verdade sobre controlado/estoque/preço).
        Optional<Produto> cadastrado = produtoRepository.findByNomeIgnoreCase(nomeProduto.trim());
        if (cadastrado.isEmpty()) {
            return new VendaResponse("NEGADA", null, null, null, "produto nao cadastrado");
        }
        Produto produto = cadastrado.get();

        if (!cpfValidator.validar(cpf)) {
            return new VendaResponse("NEGADA", null, null, null, "CPF invalido");
        }

        // Disponibilidade no fornecedor (componente externo) e estoque local da farmácia.
        if (!fornecedor.consultar(produto.getNome())) {
            return new VendaResponse("NEGADA", null, null, null, "produto indisponivel no fornecedor");
        }
        if (produto.getEstoque() <= 0) {
            return new VendaResponse("NEGADA", null, null, null, "produto sem estoque");
        }

        NotaFiscal nota = new NotaFiscal(UUID.randomUUID().toString(), cpf, produto.getNome());
        String protocoloSefaz = sefaz.enviarNota(nota);

        String protocoloAns = null;
        if (produto.isControlado()) {
            protocoloAns = ans.enviarReceita(cpf, produto.getNome());
        }

        // Baixa de estoque do produto cadastrado.
        produto.setEstoque(produto.getEstoque() - 1);
        produtoRepository.save(produto);

        // Persiste a venda autorizada no banco (NEGADAs não são gravadas, como antes).
        Venda venda = new Venda("AUTORIZADA", cpf, produto.getNome(), nota.id(),
                protocoloSefaz, protocoloAns, null, LocalDateTime.now());
        repository.save(venda);

        return new VendaResponse("AUTORIZADA", nota, protocoloSefaz, protocoloAns, null);
    }

    public List<VendaResponse> listarNotas() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /** Reconstrói o DTO de resposta a partir da entidade persistida. */
    private VendaResponse toResponse(Venda v) {
        NotaFiscal nota = new NotaFiscal(v.getNotaId(), v.getCpf(), v.getProduto());
        return new VendaResponse(v.getStatus(), nota, v.getProtocoloSefaz(),
                v.getProtocoloAns(), v.getMotivo());
    }
}
