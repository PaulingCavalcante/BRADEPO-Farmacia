package com.farmacia.dto;

/** Dados de entrada para cadastrar/atualizar um cliente. */
public record ClienteRequest(String cpf, String nome, Boolean idoso, Boolean convenio) {
}
