package com.farmacia.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Cliente cadastrado da farmácia.
 *
 * <p>O cadastro é o que habilita os descontos (Fase 4): cliente cadastrado tem
 * desconto progressivo, e quem é {@code idoso} E tem {@code convenio} ganha a
 * vantagem extra. Para produtos controlados o CPF é obrigatório (Fase 3), mas
 * o cliente não precisa estar previamente cadastrado — basta um CPF válido.</p>
 */
@Entity
@Table(name = "cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cpf;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private boolean idoso;

    @Column(nullable = false)
    private boolean convenio;

    /** Construtor sem argumentos exigido pelo JPA. */
    protected Cliente() {
    }

    public Cliente(String cpf, String nome, boolean idoso, boolean convenio) {
        this.cpf = cpf;
        this.nome = nome;
        this.idoso = idoso;
        this.convenio = convenio;
    }

    public Long getId() {
        return id;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public boolean isIdoso() {
        return idoso;
    }

    public void setIdoso(boolean idoso) {
        this.idoso = idoso;
    }

    public boolean isConvenio() {
        return convenio;
    }

    public void setConvenio(boolean convenio) {
        this.convenio = convenio;
    }
}
