package com.saniblue.app.data.repository

import com.saniblue.app.data.local.dao.HidrometroModeloDao
import com.saniblue.app.data.local.entity.HidrometroModeloEntity
import com.saniblue.app.domain.model.HidrometroModelo
import com.saniblue.app.domain.repository.HidrometroRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HidrometroRepositoryImpl @Inject constructor(
    private val dao: HidrometroModeloDao
) : HidrometroRepository {

    override fun getAll(): Flow<List<HidrometroModelo>> =
        dao.getAllAtivos().map { list -> list.map { it.toDomain() } }

    override suspend fun getById(id: Long): HidrometroModelo? =
        dao.getById(id)?.toDomain()

    override suspend fun save(modelo: HidrometroModelo): Long {
        val entity = modelo.toEntity()
        return if (modelo.id == 0L) {
            dao.insert(entity)
        } else {
            dao.update(entity)
            modelo.id
        }
    }

    override suspend fun delete(modelo: HidrometroModelo) {
        dao.delete(modelo.toEntity())
    }

    override suspend fun count(): Int = dao.count()

    private fun HidrometroModeloEntity.toDomain() = HidrometroModelo(
        id = id,
        nome = nome,
        descricao = descricao,
        vazaoNominal = vazaoNominal,
        vazaoTransicao = vazaoTransicao,
        vazaoMinima = vazaoMinima,
        limiteNominalMin = limiteNominalMin,
        limiteNominalMax = limiteNominalMax,
        limiteTransicaoMin = limiteTransicaoMin,
        limiteTransicaoMax = limiteTransicaoMax,
        limiteMinimaMin = limiteMinimaMin,
        limiteMinimaMax = limiteMinimaMax,
        ativo = ativo
    )

    private fun HidrometroModelo.toEntity() = HidrometroModeloEntity(
        id = id,
        nome = nome,
        descricao = descricao,
        vazaoNominal = vazaoNominal,
        vazaoTransicao = vazaoTransicao,
        vazaoMinima = vazaoMinima,
        limiteNominalMin = limiteNominalMin,
        limiteNominalMax = limiteNominalMax,
        limiteTransicaoMin = limiteTransicaoMin,
        limiteTransicaoMax = limiteTransicaoMax,
        limiteMinimaMin = limiteMinimaMin,
        limiteMinimaMax = limiteMinimaMax,
        ativo = ativo
    )
}
