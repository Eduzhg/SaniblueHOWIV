package com.saniblue.app.domain.repository

import com.saniblue.app.domain.model.DashboardStats
import com.saniblue.app.domain.model.Ensaio
import com.saniblue.app.domain.model.ResumoFotosEnsaio
import kotlinx.coroutines.flow.Flow

interface EnsaioRepository {
    fun getAll(): Flow<List<Ensaio>>
    fun search(query: String): Flow<List<Ensaio>>
    suspend fun getById(id: Long): Ensaio?
    suspend fun save(ensaio: Ensaio): Long
    suspend fun delete(id: Long)
    /** Apaga todos os ensaios do banco local (uso de teste/manutenção). */
    suspend fun deleteAll()
    fun getDashboardStats(): Flow<DashboardStats>

    /** Ensaios que ainda guardam fotos de leitura, com a contagem de cada um. */
    suspend fun getResumoFotosLeitura(): List<ResumoFotosEnsaio>

    /**
     * Zera no banco os caminhos das fotos de leitura e devolve os arquivos que
     * ficaram órfãos, para quem chamou apagá-los do disco.
     *
     * @param ensaioId null limpa todos os ensaios.
     */
    suspend fun limparFotosLeitura(ensaioId: Long?): List<String>
}
