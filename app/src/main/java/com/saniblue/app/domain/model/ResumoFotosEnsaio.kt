package com.saniblue.app.domain.model

/** Quantas fotos de leitura um ensaio guarda — usado na tela de Manutenção. */
data class ResumoFotosEnsaio(
    val ensaioId: Long,
    val numeroHidrometro: String,
    val dataEnsaio: String,
    val quantidadeFotos: Int
)
