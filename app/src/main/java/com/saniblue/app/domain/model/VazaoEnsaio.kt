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
    // Fotos das leituras (auditoria em campo) — caminho do JPG, "" se nao fotografou.
    // Servem so para o tecnico conferir a leitura depois; nao entram no laudo.
    val m1FotoPadraoInicial: String = "",
    val m1FotoPadraoFinal: String = "",
    val m1FotoLeituraInicial: String = "",
    val m1FotoLeituraFinal: String = "",
    val m2FotoPadraoInicial: String = "",
    val m2FotoPadraoFinal: String = "",
    val m2FotoLeituraInicial: String = "",
    val m2FotoLeituraFinal: String = "",
    val m3FotoPadraoInicial: String = "",
    val m3FotoPadraoFinal: String = "",
    val m3FotoLeituraInicial: String = "",
    val m3FotoLeituraFinal: String = "",
    // Calculados
    val erro1: Double = 0.0,
    val erro2: Double = 0.0,
    val erro3: Double = 0.0,
    val erroMedio: Double = 0.0,
    val aprovado: Boolean = false,
    // Vazão de referência não atingida em campo (ex.: pressão insuficiente) — o
    // técnico registra a vazão real utilizada no teste, para constar no laudo
    val vazaoNaoAtingida: Boolean = false,
    val vazaoUtilizada: Double = 0.0
)

/** Qual leitura da medicao a foto registra (usado no rotulo e no nome do arquivo). */
enum class CampoLeitura(val rotulo: String, val curto: String) {
    PADRAO_INICIAL("Padrão Inicial", "padrao_inicial"),
    PADRAO_FINAL("Padrão Final", "padrao_final"),
    LEITURA_INICIAL("Leitura Inicial", "leitura_inicial"),
    LEITURA_FINAL("Leitura Final", "leitura_final")
}

enum class TipoVazao(val label: String, val litrosEnsaio: Int) {
    // Volume de água exigido por medição — fixo, independe do hidrômetro, da norma
    // ou do método de ensaio.
    NOMINAL("Vazão Nominal", litrosEnsaio = 10),
    TRANSICAO("Vazão de Transição", litrosEnsaio = 5),
    MINIMA("Vazão Mínima", litrosEnsaio = 2)
}
