package com.farmacia.dto;

/** Representação de um cliente retornada pela API. */
public record ClienteResponse(Long id, String cpf, String nome, boolean idoso, boolean convenio) {
}
