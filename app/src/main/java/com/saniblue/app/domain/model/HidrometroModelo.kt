package com.saniblue.app.domain.model

/**
 * Catálogo fixo de capacidades de hidrômetro, resolvido automaticamente a partir
 * do nº de série do ensaio (norma + 1ª letra + classe R quando aplicável).
 * Ver [com.saniblue.app.data.local.database.DatabasePrePopulate] para os valores.
 */
data class HidrometroModelo(
    val id: Long = 0,
    val nome: String,
    val descricao: String = "",
    val norma: NormaEnsaio,
    // 1ª letra do nº de série — identifica a capacidade (Y/A na 246; Y/Z/A na 155)
    val letra: Char,
    // Classe metrológica (R80/R100/R125) — só na Portaria 155; null na 246
    val classeR: ClasseHidrometro? = null,
    val vazaoNominal: Double,       // L/h (QN ou Q3)
    val vazaoTransicao: Double,     // L/h (QT ou Q2)
    val vazaoMinima: Double,        // L/h (QM ou Q1)
    val ativo: Boolean = true
)
