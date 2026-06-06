package com.saniblue.app.domain.model

data class HidrometroModelo(
    val id: Long = 0,
    val nome: String,
    val descricao: String = "",
    val vazaoNominal: Double,       // L/h
    val vazaoTransicao: Double,     // L/h
    val vazaoMinima: Double,        // L/h
    val limiteNominalMin: Double = -5.0,
    val limiteNominalMax: Double = 5.0,
    val limiteTransicaoMin: Double = -5.0,
    val limiteTransicaoMax: Double = 5.0,
    val limiteMinimaMin: Double = -10.0,
    val limiteMinimaMax: Double = 10.0,
    val ativo: Boolean = true
)
