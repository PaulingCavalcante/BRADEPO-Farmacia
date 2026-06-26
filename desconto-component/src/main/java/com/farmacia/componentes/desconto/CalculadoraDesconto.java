package com.farmacia.componentes.desconto;

/**
 * Componente reutilizável de cálculo de desconto.
 *
 * <p>Não conhece JPA nem o domínio do farmacia-service: recebe apenas dados
 * simples ({@link DescontoContexto}) e devolve o resultado ({@link DescontoResultado}).
 * Assim pode ser reutilizado nas duas versões do sistema (in-process e, na v2,
 * via mensageria).</p>
 */
public interface CalculadoraDesconto {

    DescontoResultado calcular(DescontoContexto contexto);
}
