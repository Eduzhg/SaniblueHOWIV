package com.saniblue.app.domain.model

/**
 * Maleta de verificação (kit do técnico) contendo o hidrômetro padrão ultrassônico.
 *
 * Cada maleta vem com um certificado de fábrica informando o **erro padrão** do
 * seu hidrômetro ultrassônico. Esse erro NÃO é único: varia conforme a vazão
 * (nominal, transição e mínima têm valores diferentes no certificado). Ele corrige
 * o volume de referência: escoamentoCorrigido = escoamento * (100 - erroPadrao) / 100
 * (positivo diminui, negativo aumenta).
 *
 * Os valores são embutidos no build de cada maleta (ver build.gradle.kts).
 */
data class Maleta(
    val id: String,
    val nome: String,
    val erroPadraoNominal: Double,
    val erroPadraoTransicao: Double,
    val erroPadraoMinima: Double
) {
    /** Erro padrão (%) do certificado para a vazão informada. */
    fun erroPadraoPara(tipo: TipoVazao): Double = when (tipo) {
        TipoVazao.NOMINAL -> erroPadraoNominal
        TipoVazao.TRANSICAO -> erroPadraoTransicao
        TipoVazao.MINIMA -> erroPadraoMinima
    }
}
