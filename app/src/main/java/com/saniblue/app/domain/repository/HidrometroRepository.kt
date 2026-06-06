package com.saniblue.app.domain.repository

import com.saniblue.app.domain.model.HidrometroModelo
import kotlinx.coroutines.flow.Flow

interface HidrometroRepository {
    fun getAll(): Flow<List<HidrometroModelo>>
    suspend fun getById(id: Long): HidrometroModelo?
    suspend fun save(modelo: HidrometroModelo): Long
    suspend fun delete(modelo: HidrometroModelo)
    suspend fun count(): Int
}
