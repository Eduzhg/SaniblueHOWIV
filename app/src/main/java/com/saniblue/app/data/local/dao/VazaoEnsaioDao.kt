package com.saniblue.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saniblue.app.data.local.entity.VazaoEnsaioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VazaoEnsaioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(vazao: VazaoEnsaioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vazoes: List<VazaoEnsaioEntity>)

    @Update
    suspend fun update(vazao: VazaoEnsaioEntity)

    @Delete
    suspend fun delete(vazao: VazaoEnsaioEntity)

    @Query("SELECT * FROM vazao_ensaios WHERE ensaio_id = :ensaioId ORDER BY tipo_vazao ASC")
    fun getByEnsaioId(ensaioId: Long): Flow<List<VazaoEnsaioEntity>>

    @Query("SELECT * FROM vazao_ensaios WHERE ensaio_id = :ensaioId ORDER BY tipo_vazao ASC")
    suspend fun getByEnsaioIdSync(ensaioId: Long): List<VazaoEnsaioEntity>

    @Query("DELETE FROM vazao_ensaios WHERE ensaio_id = :ensaioId")
    suspend fun deleteByEnsaioId(ensaioId: Long)
}
