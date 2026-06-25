package com.farmacia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Entidade JPA que persiste a venda autorizada.
 *
 * <p>Espelha o que antes era guardado em memória como {@code VendaResponse}.
 * Como {@code NotaFiscal} é um {@code record} do componente-jar sefaz (e
 * componentes não conhecem JPA — persistência mora só no farmacia-service),
 * a nota é mapeada em colunas diretas: {@code notaId}, {@code cpf}, {@code produto}.</p>
 *
 * <p>Convenção do projeto: entidade JPA é classe normal (record não funciona
 * bem como {@code @Entity}).</p>
 */
@Entity
@Table(name = "venda")
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;

    private String cpf;

    private String produto;

    /** Identificador (UUID) da NotaFiscal gerada para esta venda. */
    @Column(name = "nota_id")
    private String notaId;

    @Column(name = "protocolo_sefaz")
    private String protocoloSefaz;

    @Column(name = "protocolo_ans")
    private String protocoloAns;

    private String motivo;

    @Column(name = "data_hora")
    private LocalDateTime dataHora;

    /** Construtor sem argumentos exigido pelo JPA. */
    protected Venda() {
    }

    public Venda(String status, String cpf, String produto, String notaId,
                 String protocoloSefaz, String protocoloAns, String motivo,
                 LocalDateTime dataHora) {
        this.status = status;
        this.cpf = cpf;
        this.produto = produto;
        this.notaId = notaId;
        this.protocoloSefaz = protocoloSefaz;
        this.protocoloAns = protocoloAns;
        this.motivo = motivo;
        this.dataHora = dataHora;
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public String getCpf() {
        return cpf;
    }

    public String getProduto() {
        return produto;
    }

    public String getNotaId() {
        return notaId;
    }

    public String getProtocoloSefaz() {
        return protocoloSefaz;
    }

    public String getProtocoloAns() {
        return protocoloAns;
    }

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
