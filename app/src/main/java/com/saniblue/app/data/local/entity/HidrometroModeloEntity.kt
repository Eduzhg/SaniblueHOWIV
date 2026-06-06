package com.saniblue.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hidrometro_modelos")
data class HidrometroModeloEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo("nome") val nome: String,
    @ColumnInfo("descricao") val descricao: String = "",
    // Vazões em L/h
    @ColumnInfo("vazao_nominal") val vazaoNominal: Double,
    @ColumnInfo("vazao_transicao") val vazaoTransicao: Double,
    @ColumnInfo("vazao_minima") val vazaoMinima: Double,
    // Limites de erro para Vazão Nominal (%)
    @ColumnInfo("limite_nominal_min") val limiteNominalMin: Double = -5.0,
    @ColumnInfo("limite_nominal_max") val limiteNominalMax: Double = 5.0,
    // Limites de erro para Vazão de Transição (%)
    @ColumnInfo("limite_transicao_min") val limiteTransicaoMin: Double = -5.0,
    @ColumnInfo("limite_transicao_max") val limiteTransicaoMax: Double = 5.0,
    // Limites de erro para Vazão Mínima (%)
    @ColumnInfo("limite_minima_min") val limiteMinimaMin: Double = -10.0,
    @ColumnInfo("limite_minima_max") val limiteMinimaMax: Double = 10.0,
    @ColumnInfo("ativo") val ativo: Boolean = true,
    @ColumnInfo("created_at") val createdAt: Long = System.currentTimeMillis()
)
