package com.farmacia.service;

import com.farmacia.dto.ProdutoRequest;
import com.farmacia.dto.ProdutoResponse;
import com.farmacia.model.Categoria;
import com.farmacia.model.Produto;
import com.farmacia.repository.ProdutoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

/** Regras de cadastro (CRUD) de produtos. */
@Service
public class ProdutoService {

    private final ProdutoRepository repository;

    public ProdutoService(ProdutoRepository repository) {
        this.repository = repository;
    }

    public ProdutoResponse criar(ProdutoRequest req) {
        validar(req);
        repository.findByNomeIgnoreCase(req.nome()).ifPresent(p -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe produto com o nome '" + req.nome() + "'");
        });
        Produto produto = new Produto(
                req.nome().trim(),
                Categoria.valueOf(req.categoria().trim().toUpperCase()),
                req.preco(),
                req.estoque(),
                Boolean.TRUE.equals(req.controlado()));
        return toResponse(repository.save(produto));
    }

    public List<ProdutoResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public ProdutoResponse buscar(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public ProdutoResponse atualizar(Long id, ProdutoRequest req) {
        validar(req);
        Produto produto = buscarEntidade(id);
        produto.setNome(req.nome().trim());
        produto.setCategoria(Categoria.valueOf(req.categoria().trim().toUpperCase()));
        produto.setPreco(req.preco());
        produto.setEstoque(req.estoque());
        produto.setControlado(Boolean.TRUE.equals(req.controlado()));
        return toResponse(repository.save(produto));
    }

    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto " + id + " não encontrado");
        }
        repository.deleteById(id);
    }

    private Produto buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto " + id + " não encontrado"));
    }

    /** Validações de campos obrigatórios; lança 400 com mensagem clara. */
    private void validar(ProdutoRequest req) {
        if (req.nome() == null || req.nome().isBlank()) {
            erro("nome é obrigatório");
        }
        if (req.categoria() == null || req.categoria().isBlank()) {
            erro("categoria é obrigatória (MEDICAMENTO ou HIGIENE)");
        } else {
            try {
                Categoria.valueOf(req.categoria().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                erro("categoria inválida: use MEDICAMENTO ou HIGIENE");
            }
        }
        if (req.preco() == null || req.preco().compareTo(BigDecimal.ZERO) < 0) {
            erro("preco é obrigatório e não pode ser negativo");
        }
        if (req.estoque() == null || req.estoque() < 0) {
            erro("estoque é obrigatório e não pode ser negativo");
        }
    }

    private void erro(String msg) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    private ProdutoResponse toResponse(Produto p) {
        return new ProdutoResponse(p.getId(), p.getNome(), p.getCategoria().name(),
                p.getPreco(), p.getEstoque(), p.isControlado());
    }
}
