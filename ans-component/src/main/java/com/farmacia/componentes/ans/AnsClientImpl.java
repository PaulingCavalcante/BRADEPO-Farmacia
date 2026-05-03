package com.farmacia.componentes.ans;

import org.springframework.stereotype.Component;

@Component
public class AnsClientImpl implements AnsClient {

    @Override
    public String enviarReceita(String cpf, String produto) {
        String protocolo = "ANS-" + System.currentTimeMillis();
        System.out.println("[ANS] Receita registrada (cpf=" + cpf + ", produto=" + produto + ") -> " + protocolo);
        return protocolo;
    }
}
