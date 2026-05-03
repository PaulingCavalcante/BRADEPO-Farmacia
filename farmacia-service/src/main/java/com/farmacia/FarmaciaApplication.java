package com.farmacia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.farmacia.componentes.ans.AnsClient;
import com.farmacia.componentes.cpf.CpfValidator;
import com.farmacia.componentes.fornecedor.FornecedorAdapter;
import com.farmacia.componentes.sefaz.NotaFiscal;
import com.farmacia.componentes.sefaz.SefazClient;

@SpringBootApplication
@RestController
public class FarmaciaApplication {

    private static final Set<String> CONTROLADOS = Set.of("rivotril", "diazepam", "ritalina");

    private final CpfValidator cpfValidator;
    private final FornecedorAdapter fornecedor;
    private final SefazClient sefaz;
    private final AnsClient ans;

    private final List<Map<String, Object>> notas = new ArrayList<>();

    public FarmaciaApplication(CpfValidator cpfValidator,
                               FornecedorAdapter fornecedor,
                               SefazClient sefaz,
                               AnsClient ans) {
        this.cpfValidator = cpfValidator;
        this.fornecedor = fornecedor;
        this.sefaz = sefaz;
        this.ans = ans;
    }

    public static void main(String[] args) {
        SpringApplication.run(FarmaciaApplication.class, args);
    }

    @PostMapping("/venda")
    public Map<String, Object> vender(@RequestBody Map<String, String> req) {
        String cpf = req.get("cpf");
        String produto = req.get("produto");

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

        notas.add(resposta);
        return resposta;
    }

    @GetMapping("/notas")
    public List<Map<String, Object>> listar() {
        return notas;
    }
}
