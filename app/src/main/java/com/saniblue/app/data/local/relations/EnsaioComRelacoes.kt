package com.saniblue.app.data.local.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.saniblue.app.data.local.entity.EnsaioEntity
import com.saniblue.app.data.local.entity.VazaoEnsaioEntity

data class EnsaioComRelacoes(
    @Embedded val ensaio: EnsaioEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "ensaio_id"
    )
    val vazoes: List<VazaoEnsaioEntity>
)
