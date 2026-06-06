package com.saniblue.app.domain.model

data class FotoEnsaio(
    val id: Long = 0,
    val tipoFoto: TipoFoto,
    val caminhoArquivo: String,
    val dataCaptura: Long = System.currentTimeMillis()
)

enum class TipoFoto(val label: String) {
    HIDROMETRO("Hidrômetro"),
    LOCAL("Local"),
    LEITURA("Leitura"),
    ASSINATURA("Assinatura")
}
