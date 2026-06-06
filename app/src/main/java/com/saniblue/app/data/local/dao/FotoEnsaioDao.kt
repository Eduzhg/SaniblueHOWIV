package com.saniblue.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.saniblue.app.data.local.entity.FotoEnsaioEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FotoEnsaioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foto: FotoEnsaioEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fotos: List<FotoEnsaioEntity>)

    @Delete
    suspend fun delete(foto: FotoEnsaioEntity)

    @Query("SELECT * FROM fotos_ensaio WHERE ensaio_id = :ensaioId ORDER BY data_captura ASC")
    fun getByEnsaioId(ensaioId: Long): Flow<List<FotoEnsaioEntity>>

    @Query("SELECT * FROM fotos_ensaio WHERE ensaio_id = :ensaioId ORDER BY data_captura ASC")
    suspend fun getByEnsaioIdSync(ensaioId: Long): List<FotoEnsaioEntity>

    @Query("DELETE FROM fotos_ensaio WHERE ensaio_id = :ensaioId")
    suspend fun deleteByEnsaioId(ensaioId: Long)
}
