package com.saniblue.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.saniblue.app.data.local.entity.EnsaioEntity
import com.saniblue.app.data.local.relations.EnsaioComRelacoes
import kotlinx.coroutines.flow.Flow

@Dao
interface EnsaioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(ensaio: EnsaioEntity): Long

    @Update
    suspend fun update(ensaio: EnsaioEntity)

    @Delete
    suspend fun delete(ensaio: EnsaioEntity)

    /** Apaga todos os ensaios (as vazões caem em cascata via ForeignKey). */
    @Query("DELETE FROM ensaios")
    suspend fun deleteAll()

    @Query("SELECT * FROM ensaios WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): EnsaioEntity?

    @Transaction
    @Query("SELECT * FROM ensaios WHERE id = :id LIMIT 1")
    suspend fun getComRelacoesByid(id: Long): EnsaioComRelacoes?

    @Transaction
    @Query("SELECT * FROM ensaios ORDER BY created_at DESC")
    fun getAllComRelacoes(): Flow<List<EnsaioComRelacoes>>

    @Transaction
    @Query("""
        SELECT * FROM ensaios
        WHERE (:query = '' OR numero_hidrometro LIKE '%' || :query || '%'
            OR cliente LIKE '%' || :query || '%')
        ORDER BY created_at DESC
    """)
    fun searchComRelacoes(query: String): Flow<List<EnsaioComRelacoes>>

    @Query("SELECT COUNT(*) FROM ensaios")
    fun countTotal(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ensaios WHERE resultado_final = 'APROVADO'")
    fun countAprovados(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ensaios WHERE resultado_final = 'REPROVADO'")
    fun countReprovados(): Flow<Int>

    @Query("SELECT COUNT(*) FROM ensaios WHERE resultado_final = 'PENDENTE'")
    fun countPendentes(): Flow<Int>

    @Query("UPDATE ensaios SET resultado_final = :resultado, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateResultado(id: Long, resultado: String, updatedAt: Long = System.currentTimeMillis())
}
