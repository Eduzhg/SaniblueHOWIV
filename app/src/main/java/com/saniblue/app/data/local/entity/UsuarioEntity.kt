package com.saniblue.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["login"], unique = true)]
)
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo("nome") val nome: String,
    @ColumnInfo("login") val login: String,
    @ColumnInfo("senha_hash") val senhaHash: String,
    @ColumnInfo("cargo") val cargo: String = "Técnico",
    @ColumnInfo("email") val email: String = "",
    @ColumnInfo("ativo") val ativo: Boolean = true,
    @ColumnInfo("created_at") val createdAt: Long = System.currentTimeMillis()
)
