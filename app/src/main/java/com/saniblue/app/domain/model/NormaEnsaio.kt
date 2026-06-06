package com.saniblue.app.domain.model

/**
 * Norma metrológica usada no ensaio.
 *
 * A norma define DOIS aspectos:
 *  1. A nomenclatura das 3 vazões (QN/QT/QM na 246  ↔  Q3/Q2/Q1 na 155).
 *  2. Os limites de erro aceitáveis por vazão.
 *
 * O modelo de hidrômetro (HidrometroModelo) continua fornecendo apenas os
 * VALORES de vazão de referência (L/h). Os limites vêm daqui.
 *
 * ⚠️ ATENÇÃO: os limites da PORTARIA_155 abaixo são um ponto de partida e
 * precisam ser confirmados com os valores oficiais em serviço. Basta editar
 * os números deste enum — nenhuma outra alteração de código é necessária.
 */
enum class NormaEnsaio(
    val label: String,
    val descricao: String,
    // Rótulos por tipo de vazão
    val labelNominal: String,
    val labelTransicao: String,
    val labelMinima: String,
    // Limites de erro (%) por tipo de vazão
    val limiteNominalMin: Double,
    val limiteNominalMax: Double,
    val limiteTransicaoMin: Double,
    val limiteTransicaoMax: Double,
    val limiteMinimaMin: Double,
    val limiteMinimaMax: Double
) {
    PORTARIA_246(
        label = "Portaria 246",
        descricao = "Portaria Inmetro 246 — QN / QT / QM",
        labelNominal = "Vazão Nominal (QN)",
        labelTransicao = "Vazão de Transição (QT)",
        labelMinima = "Vazão Mínima (QM)",
        limiteNominalMin = -5.0, limiteNominalMax = 5.0,
        limiteTransicaoMin = -5.0, limiteTransicaoMax = 5.0,
        limiteMinimaMin = -10.0, limiteMinimaMax = 10.0
    ),

    PORTARIA_155(
        label = "Portaria 155",
        descricao = "Portaria Inmetro 155 — Q3 / Q2 / Q1",
        labelNominal = "Vazão Permanente (Q3)",
        labelTransicao = "Vazão de Transição (Q2)",
        labelMinima = "Vazão Mínima (Q1)",
        // TODO: confirmar limites oficiais da Portaria 155 em serviço
        limiteNominalMin = -5.0, limiteNominalMax = 5.0,
        limiteTransicaoMin = -5.0, limiteTransicaoMax = 5.0,
        limiteMinimaMin = -10.0, limiteMinimaMax = 10.0
    );

    fun labelPara(tipo: TipoVazao): String = when (tipo) {
        TipoVazao.NOMINAL -> labelNominal
        TipoVazao.TRANSICAO -> labelTransicao
        TipoVazao.MINIMA -> labelMinima
    }

    fun limiteMin(tipo: TipoVazao): Double = when (tipo) {
        TipoVazao.NOMINAL -> limiteNominalMin
        TipoVazao.TRANSICAO -> limiteTransicaoMin
        TipoVazao.MINIMA -> limiteMinimaMin
    }

    fun limiteMax(tipo: TipoVazao): Double = when (tipo) {
        TipoVazao.NOMINAL -> limiteNominalMax
        TipoVazao.TRANSICAO -> limiteTransicaoMax
        TipoVazao.MINIMA -> limiteMinimaMax
    }

    fun limiteLabel(tipo: TipoVazao): String =
        "${limiteMin(tipo)}% a +${limiteMax(tipo)}%"
}

/**
 * Forma de obtenção do volume escoado no ensaio.
 *
 *  - ESCOAMENTO_DIRETO: o técnico informa o volume escoado (L) diretamente,
 *    junto com a leitura inicial/final do hidrômetro de campo.
 *
 *  - COMPARATIVO_LEITURA: a maleta de verificação (padrão ultrassônico) não
 *    zera. O técnico anota a leitura inicial e final do padrão; o volume
 *    escoado é CALCULADO = leituraFinalPadrão − leituraInicialPadrão.
 */
enum class MetodoEnsaio(val label: String, val descricao: String) {
    ESCOAMENTO_DIRETO(
        "Escoamento direto",
        "Volume escoado informado diretamente"
    ),
    COMPARATIVO_LEITURA(
        "Comparativo por leitura",
        "Volume calculado pela leitura do padrão ultrassônico"
    )
}
