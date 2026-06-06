package com.saniblue.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vazao_ensaios",
    foreignKeys = [
        ForeignKey(
            entity = EnsaioEntity::class,
            parentColumns = ["id"],
            childColumns = ["ensaio_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ensaio_id")]
)
data class VazaoEnsaioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo("ensaio_id") val ensaioId: Long,

    // NOMINAL | TRANSICAO | MINIMA
    @ColumnInfo("tipo_vazao") val tipoVazao: String,

    // Medição 1: Escoamento (L), Leitura Inicial, Leitura Final
    @ColumnInfo("m1_escoamento") val m1Escoamento: Double = 0.0,
    @ColumnInfo("m1_leitura_inicial") val m1LeituraInicial: Double = 0.0,
    @ColumnInfo("m1_leitura_final") val m1LeituraFinal: Double = 0.0,

    // Medição 2
    @ColumnInfo("m2_escoamento") val m2Escoamento: Double = 0.0,
    @ColumnInfo("m2_leitura_inicial") val m2LeituraInicial: Double = 0.0,
    @ColumnInfo("m2_leitura_final") val m2LeituraFinal: Double = 0.0,

    // Medição 3
    @ColumnInfo("m3_escoamento") val m3Escoamento: Double = 0.0,
    @ColumnInfo("m3_leitura_inicial") val m3LeituraInicial: Double = 0.0,
    @ColumnInfo("m3_leitura_final") val m3LeituraFinal: Double = 0.0,

    // Leituras do padrão ultrassônico (método COMPARATIVO_LEITURA)
    @ColumnInfo("m1_padrao_inicial") val m1PadraoInicial: Double = 0.0,
    @ColumnInfo("m1_padrao_final") val m1PadraoFinal: Double = 0.0,
    @ColumnInfo("m2_padrao_inicial") val m2PadraoInicial: Double = 0.0,
    @ColumnInfo("m2_padrao_final") val m2PadraoFinal: Double = 0.0,
    @ColumnInfo("m3_padrao_inicial") val m3PadraoInicial: Double = 0.0,
    @ColumnInfo("m3_padrao_final") val m3PadraoFinal: Double = 0.0,

    // Resultados calculados e persistidos para histórico
    @ColumnInfo("erro_1") val erro1: Double = 0.0,
    @ColumnInfo("erro_2") val erro2: Double = 0.0,
    @ColumnInfo("erro_3") val erro3: Double = 0.0,
    @ColumnInfo("erro_medio") val erroMedio: Double = 0.0,
    @ColumnInfo("aprovado") val aprovado: Boolean = false
)
