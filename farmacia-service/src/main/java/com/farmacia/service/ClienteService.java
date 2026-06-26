package com.farmacia.service;

import com.farmacia.componentes.cpf.CpfValidator;
import com.farmacia.dto.ClienteRequest;
import com.farmacia.dto.ClienteResponse;
import com.farmacia.model.Cliente;
import com.farmacia.repository.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Regras de cadastro (CRUD) de clientes. Reutiliza o componente CpfValidator. */
@Service
public class ClienteService {

    private final ClienteRepository repository;
    private final CpfValidator cpfValidator;

    public ClienteService(ClienteRepository repository, CpfValidator cpfValidator) {
        this.repository = repository;
        this.cpfValidator = cpfValidator;
    }

    public ClienteResponse criar(ClienteRequest req) {
        validar(req);
        repository.findByCpf(req.cpf().trim()).ifPresent(c -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe cliente com o CPF " + req.cpf());
        });
        Cliente cliente = new Cliente(
                req.cpf().trim(),
                req.nome().trim(),
                Boolean.TRUE.equals(req.idoso()),
                Boolean.TRUE.equals(req.convenio()));
        return toResponse(repository.save(cliente));
    }

    public List<ClienteResponse> listar() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    public ClienteResponse buscar(Long id) {
        return toResponse(buscarEntidade(id));
    }

    public ClienteResponse atualizar(Long id, ClienteRequest req) {
        validar(req);
        Cliente cliente = buscarEntidade(id);
        cliente.setCpf(req.cpf().trim());
        cliente.setNome(req.nome().trim());
        cliente.setIdoso(Boolean.TRUE.equals(req.idoso()));
        cliente.setConvenio(Boolean.TRUE.equals(req.convenio()));
        return toResponse(repository.save(cliente));
    }

    public void remover(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente " + id + " não encontrado");
        }
        repository.deleteById(id);
    }

    private Cliente buscarEntidade(Long id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente " + id + " não encontrado"));
    }

    private void validar(ClienteRequest req) {
        if (req.nome() == null || req.nome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nome é obrigatório");
        }
        if (!cpfValidator.validar(req.cpf())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CPF inválido");
        }
    }

    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(c.getId(), c.getCpf(), c.getNome(), c.isIdoso(), c.isConvenio());
    }
}
