package com.farmacia.componentes.fornecedor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class FornecedorAdapterImpl implements FornecedorAdapter {

    private static final Set<String> SEM_ESTOQUE = Set.of("ibuprofeno");

    private static final Path SPOOL = Path.of(
        System.getProperty("java.io.tmpdir"), "fornecedor-legado.log");

    @Override
    public boolean consultar(String produto) {
        boolean disponivel = produto != null && !SEM_ESTOQUE.contains(produto.toLowerCase());
        registrarSpool(produto, disponivel);
        return disponivel;
    }

    private void registrarSpool(String produto, boolean disponivel) {
        String linha = LocalDateTime.now() + "|" + produto + "|" + (disponivel ? "OK" : "SEM_ESTOQUE") + System.lineSeparator();
        try {
            Files.writeString(SPOOL, linha,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[FORNECEDOR] falha ao gravar spool legado: " + e.getMessage());
        }
    }
}
