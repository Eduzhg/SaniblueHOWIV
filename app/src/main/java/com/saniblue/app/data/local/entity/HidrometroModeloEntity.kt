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
    // Nome do enum NormaEnsaio (PORTARIA_246 / PORTARIA_155)
    @ColumnInfo("norma") val norma: String,
    // 1ª letra do nº de série (Y/A na 246; Y/Z/A na 155)
    @ColumnInfo("letra") val letra: String,
    // Nome do enum ClasseHidrometro (R80/R100/R125), ou "" — só se aplica à 155
    @ColumnInfo("classe_r") val classeR: String = "",
    // Vazões em L/h
    @ColumnInfo("vazao_nominal") val vazaoNominal: Double,
    @ColumnInfo("vazao_transicao") val vazaoTransicao: Double,
    @ColumnInfo("vazao_minima") val vazaoMinima: Double,
    @ColumnInfo("ativo") val ativo: Boolean = true,
    @ColumnInfo("created_at") val createdAt: Long = System.currentTimeMillis()
)
