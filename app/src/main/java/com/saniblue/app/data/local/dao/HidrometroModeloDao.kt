package com.saniblue.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saniblue.app.data.local.entity.HidrometroModeloEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HidrometroModeloDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(modelo: HidrometroModeloEntity): Long

    @Update
    suspend fun update(modelo: HidrometroModeloEntity)

    @Delete
    suspend fun delete(modelo: HidrometroModeloEntity)

    @Query("SELECT * FROM hidrometro_modelos WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): HidrometroModeloEntity?

    @Query("SELECT * FROM hidrometro_modelos WHERE ativo = 1 ORDER BY nome ASC")
    fun getAllAtivos(): Flow<List<HidrometroModeloEntity>>

    @Query("SELECT * FROM hidrometro_modelos ORDER BY nome ASC")
    fun getAll(): Flow<List<HidrometroModeloEntity>>

    @Query("SELECT COUNT(*) FROM hidrometro_modelos")
    suspend fun count(): Int

    /** Resolve o modelo pela norma + letra do nº de série + classe R (155) ou "" (246). */
    @Query("""
        SELECT * FROM hidrometro_modelos
        WHERE norma = :norma AND letra = :letra AND classe_r = :classeR AND ativo = 1
        LIMIT 1
    """)
    suspend fun getByNormaLetraClasse(norma: String, letra: String, classeR: String): HidrometroModeloEntity?
}
