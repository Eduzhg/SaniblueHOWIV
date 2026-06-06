package com.saniblue.app.domain.model

data class VazaoEnsaio(
    val id: Long = 0,
    val tipoVazao: TipoVazao,
    val m1Escoamento: Double = 0.0,
    val m1LeituraInicial: Double = 0.0,
    val m1LeituraFinal: Double = 0.0,
    val m2Escoamento: Double = 0.0,
    val m2LeituraInicial: Double = 0.0,
    val m2LeituraFinal: Double = 0.0,
    val m3Escoamento: Double = 0.0,
    val m3LeituraInicial: Double = 0.0,
    val m3LeituraFinal: Double = 0.0,
    // Leituras do padrão ultrassônico (método COMPARATIVO_LEITURA).
    // O escoamento é calculado = padraoFinal − padraoInicial.
    val m1PadraoInicial: Double = 0.0,
    val m1PadraoFinal: Double = 0.0,
    val m2PadraoInicial: Double = 0.0,
    val m2PadraoFinal: Double = 0.0,
    val m3PadraoInicial: Double = 0.0,
    val m3PadraoFinal: Double = 0.0,
    // Calculados
    val erro1: Double = 0.0,
    val erro2: Double = 0.0,
    val erro3: Double = 0.0,
    val erroMedio: Double = 0.0,
    val aprovado: Boolean = false
)

enum class TipoVazao(val label: String) {
    NOMINAL("Vazão Nominal"),
    TRANSICAO("Vazão de Transição"),
    MINIMA("Vazão Mínima")
}
