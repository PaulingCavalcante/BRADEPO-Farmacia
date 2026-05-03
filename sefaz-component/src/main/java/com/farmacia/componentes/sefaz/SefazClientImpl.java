package com.farmacia.componentes.sefaz;

import org.springframework.stereotype.Component;

@Component
public class SefazClientImpl implements SefazClient {

    @Override
    public String enviarNota(NotaFiscal nota) {
        String protocolo = "SEFAZ-" + System.currentTimeMillis();
        System.out.println("[SEFAZ] Nota " + nota.id() + " autorizada -> " + protocolo);
        return protocolo;
    }
}
