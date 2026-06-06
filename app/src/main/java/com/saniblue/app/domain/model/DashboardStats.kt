package com.saniblue.app.domain.model

data class DashboardStats(
    val totalEnsaios: Int = 0,
    val aprovados: Int = 0,
    val reprovados: Int = 0,
    val pendentes: Int = 0
) {
    val taxaAprovacao: Float
        get() = if (totalEnsaios > 0) (aprovados.toFloat() / totalEnsaios) * 100f else 0f
}
