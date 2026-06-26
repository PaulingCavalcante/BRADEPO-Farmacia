package com.farmacia.componentes.desconto;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Regras de desconto:
 *
 * <ul>
 *   <li>Cliente NÃO cadastrado: sem desconto.</li>
 *   <li>Cliente cadastrado: desconto progressivo do fabricante por faixa de valor
 *       (5% até R$50, 8% de R$50 a R$150, 12% acima de R$150).</li>
 *   <li>Idoso COM convênio: ganha também o desconto de convênio (15%). Entre o
 *       desconto do convênio e o do fabricante, aplica-se a <b>maior vantagem</b>
 *       (conforme enunciado).</li>
 * </ul>
 */
@Component
public class CalculadoraDescontoImpl implements CalculadoraDesconto {

    private static final BigDecimal CEM = new BigDecimal("100");
    private static final BigDecimal FAIXA_MEDIA = new BigDecimal("50");
    private static final BigDecimal FAIXA_ALTA = new BigDecimal("150");
    private static final BigDecimal PCT_CONVENIO = new BigDecimal("15");

    @Override
    public DescontoResultado calcular(DescontoContexto ctx) {
        BigDecimal bruto = ctx.valorBruto() == null ? BigDecimal.ZERO : ctx.valorBruto();

        if (!ctx.clienteCadastrado()) {
            return montar(bruto, BigDecimal.ZERO, "Sem desconto (cliente nao cadastrado)");
        }

        BigDecimal pctFabricante = progressivo(bruto);
        BigDecimal pctConvenio = (ctx.idoso() && ctx.convenio()) ? PCT_CONVENIO : BigDecimal.ZERO;

        if (pctConvenio.compareTo(pctFabricante) > 0) {
            return montar(bruto, pctConvenio,
                    "Desconto convenio (idoso): " + pctConvenio + "% (maior vantagem)");
        }
        return montar(bruto, pctFabricante,
                "Desconto fabricante (cliente cadastrado): " + pctFabricante + "%");
    }

    /** Desconto progressivo do fabricante por faixa de valor bruto. */
    private BigDecimal progressivo(BigDecimal bruto) {
        if (bruto.compareTo(FAIXA_ALTA) >= 0) {
            return new BigDecimal("12");
        }
        if (bruto.compareTo(FAIXA_MEDIA) >= 0) {
            return new BigDecimal("8");
        }
        return new BigDecimal("5");
    }

    private DescontoResultado montar(BigDecimal bruto, BigDecimal percentual, String descricao) {
        BigDecimal valorDesconto = bruto.multiply(percentual)
                .divide(CEM, 2, RoundingMode.HALF_UP);
        BigDecimal valorLiquido = bruto.subtract(valorDesconto).setScale(2, RoundingMode.HALF_UP);
        return new DescontoResultado(
                percentual.setScale(2, RoundingMode.HALF_UP),
                valorDesconto,
                valorLiquido,
                descricao);
    }
}
