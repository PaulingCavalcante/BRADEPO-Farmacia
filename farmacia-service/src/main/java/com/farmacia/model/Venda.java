package com.farmacia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
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

    // ===== Fase 4 — valores e desconto =====
    @Column(name = "valor_bruto", precision = 10, scale = 2)
    private BigDecimal valorBruto;

    @Column(name = "percentual_desconto", precision = 10, scale = 2)
    private BigDecimal percentualDesconto;

    @Column(name = "valor_desconto", precision = 10, scale = 2)
    private BigDecimal valorDesconto;

    @Column(name = "valor_liquido", precision = 10, scale = 2)
    private BigDecimal valorLiquido;

    @Column(name = "descricao_desconto")
    private String descricaoDesconto;

    // ===== Fase 5 — canal e comissão =====
    @Enumerated(EnumType.STRING)
    private Canal canal;

    private String vendedor;

    @Column(precision = 10, scale = 2)
    private BigDecimal comissao;

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

    public BigDecimal getValorBruto() {
        return valorBruto;
    }

    public void setValorBruto(BigDecimal valorBruto) {
        this.valorBruto = valorBruto;
    }

    public BigDecimal getPercentualDesconto() {
        return percentualDesconto;
    }

    public void setPercentualDesconto(BigDecimal percentualDesconto) {
        this.percentualDesconto = percentualDesconto;
    }

    public BigDecimal getValorDesconto() {
        return valorDesconto;
    }

    public void setValorDesconto(BigDecimal valorDesconto) {
        this.valorDesconto = valorDesconto;
    }

    public BigDecimal getValorLiquido() {
        return valorLiquido;
    }

    public void setValorLiquido(BigDecimal valorLiquido) {
        this.valorLiquido = valorLiquido;
    }

    public String getDescricaoDesconto() {
        return descricaoDesconto;
    }

    public void setDescricaoDesconto(String descricaoDesconto) {
        this.descricaoDesconto = descricaoDesconto;
    }

    public Canal getCanal() {
        return canal;
    }

    public void setCanal(Canal canal) {
        this.canal = canal;
    }

    public String getVendedor() {
        return vendedor;
    }

    public void setVendedor(String vendedor) {
        this.vendedor = vendedor;
    }

    public BigDecimal getComissao() {
        return comissao;
    }

    public void setComissao(BigDecimal comissao) {
        this.comissao = comissao;
    }
}
