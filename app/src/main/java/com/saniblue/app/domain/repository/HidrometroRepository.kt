package com.saniblue.app.domain.repository

import com.saniblue.app.domain.model.ClasseHidrometro
import com.saniblue.app.domain.model.HidrometroModelo
import com.saniblue.app.domain.model.NormaEnsaio
import kotlinx.coroutines.flow.Flow

interface HidrometroRepository {
    fun getAll(): Flow<List<HidrometroModelo>>
    suspend fun getById(id: Long): HidrometroModelo?
    suspend fun save(modelo: HidrometroModelo): Long
    suspend fun delete(modelo: HidrometroModelo)
    suspend fun count(): Int
    /** Resolve o catálogo automaticamente a partir do nº de série (norma + letra + classe R). */
    suspend fun getByNormaLetraClasse(norma: NormaEnsaio, letra: Char, classeR: ClasseHidrometro?): HidrometroModelo?
}
