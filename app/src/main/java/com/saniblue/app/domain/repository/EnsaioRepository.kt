package com.saniblue.app.domain.repository

import com.saniblue.app.domain.model.DashboardStats
import com.saniblue.app.domain.model.Ensaio
import kotlinx.coroutines.flow.Flow

interface EnsaioRepository {
    fun getAll(): Flow<List<Ensaio>>
    fun search(query: String): Flow<List<Ensaio>>
    suspend fun getById(id: Long): Ensaio?
    suspend fun save(ensaio: Ensaio): Long
    suspend fun delete(id: Long)
    fun getDashboardStats(): Flow<DashboardStats>
}
