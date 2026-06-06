package com.saniblue.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fotos_ensaio",
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
data class FotoEnsaioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo("ensaio_id") val ensaioId: Long,
    // HIDROMETRO | LOCAL | LEITURA | ASSINATURA
    @ColumnInfo("tipo_foto") val tipoFoto: String,
    @ColumnInfo("caminho_arquivo") val caminhoArquivo: String,
    @ColumnInfo("data_captura") val dataCaptura: Long = System.currentTimeMillis()
)
