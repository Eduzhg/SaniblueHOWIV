package com.saniblue.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saniblue.app.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(usuario: UsuarioEntity): Long

    @Update
    suspend fun update(usuario: UsuarioEntity)

    @Query("SELECT * FROM usuarios WHERE login = :login AND senha_hash = :senhaHash AND ativo = 1 LIMIT 1")
    suspend fun autenticar(login: String, senhaHash: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE ativo = 1 ORDER BY nome ASC")
    fun getAllAtivos(): Flow<List<UsuarioEntity>>

    @Query("SELECT * FROM usuarios WHERE login = :login LIMIT 1")
    suspend fun getByLogin(login: String): UsuarioEntity?
}
