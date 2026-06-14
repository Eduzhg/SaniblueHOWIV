package com.saniblue.app.data.repository

import com.saniblue.app.data.local.dao.EnsaioDao
import com.saniblue.app.data.local.dao.VazaoEnsaioDao
import com.saniblue.app.data.local.entity.EnsaioEntity
import com.saniblue.app.data.local.entity.VazaoEnsaioEntity
import com.saniblue.app.domain.model.DashboardStats
import com.saniblue.app.domain.model.Ensaio
import com.saniblue.app.domain.model.MetodoEnsaio
import com.saniblue.app.domain.model.NormaEnsaio
import com.saniblue.app.domain.model.ResultadoFinal
import com.saniblue.app.domain.model.TipoVazao
import com.saniblue.app.domain.model.VazaoEnsaio
import com.saniblue.app.domain.repository.EnsaioRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EnsaioRepositoryImpl @Inject constructor(
    private val ensaioDao: EnsaioDao,
    private val vazaoEnsaioDao: VazaoEnsaioDao
) : EnsaioRepository {

    override fun getAll(): Flow<List<Ensaio>> =
        ensaioDao.getAllComRelacoes().map { list ->
            list.map { relacao ->
                relacao.ensaio.toDomain(vazoes = relacao.vazoes.map { it.toDomain() })
            }
        }

    override fun search(query: String): Flow<List<Ensaio>> =
        ensaioDao.searchComRelacoes(query).map { list ->
            list.map { relacao ->
                relacao.ensaio.toDomain(vazoes = relacao.vazoes.map { it.toDomain() })
            }
        }

    override suspend fun getById(id: Long): Ensaio? {
        val relacao = ensaioDao.getComRelacoesByid(id) ?: return null
        return relacao.ensaio.toDomain(vazoes = relacao.vazoes.map { it.toDomain() })
    }

    override suspend fun save(ensaio: Ensaio): Long {
        val entity = ensaio.toEntity()
        val id = if (ensaio.id == 0L) {
            ensaioDao.insert(entity)
        } else {
            ensaioDao.update(entity.copy(updatedAt = System.currentTimeMillis()))
            ensaio.id
        }

        // Replace all vazoes
        vazaoEnsaioDao.deleteByEnsaioId(id)
        val vazaoEntities = ensaio.vazoes.map { it.toEntity(id) }
        vazaoEnsaioDao.insertAll(vazaoEntities)

        // Update resultado final
        ensaioDao.updateResultado(id, ensaio.resultadoFinal.name)

        return id
    }

    override suspend fun delete(id: Long) {
        val entity = ensaioDao.getById(id) ?: return
        ensaioDao.delete(entity)
    }

    override fun getDashboardStats(): Flow<DashboardStats> =
        combine(
            ensaioDao.countTotal(),
            ensaioDao.countAprovados(),
            ensaioDao.countReprovados(),
            ensaioDao.countPendentes()
        ) { total, aprovados, reprovados, pendentes ->
            DashboardStats(total, aprovados, reprovados, pendentes)
        }

    // --- Mappers ---

    private fun EnsaioEntity.toDomain(vazoes: List<VazaoEnsaio>) = Ensaio(
        id = id,
        hidrometroModeloId = hidrometroModeloId,
        numeroHidrometro = numeroHidrometro,
        cliente = cliente,
        matricula = matricula,
        endereco = endereco,
        cidade = cidade,
        bairro = bairro,
        dataEnsaio = dataEnsaio,
        tecnicoResponsavel = tecnicoResponsavel,
        idadeHidrometro = idadeHidrometro,
        temperaturaAgua = temperaturaAgua,
        observacoes = observacoes,
        nomeCompanhia = nomeCompanhia,
        norma = runCatching { NormaEnsaio.valueOf(norma) }.getOrDefault(NormaEnsaio.PORTARIA_246),
        metodoEnsaio = runCatching { MetodoEnsaio.valueOf(metodoEnsaio) }.getOrDefault(MetodoEnsaio.ESCOAMENTO_DIRETO),
        maletaNome = maletaNome,
        erroPadrao = erroPadrao,
        pressaoMedia = pressaoMedia,
        realizado = realizado,
        motivoNaoRealizado = motivoNaoRealizado,
        leituraFinalReprovado = leituraFinalReprovado,
        numeroSerieNovo = numeroSerieNovo,
        leituraInicialNovo = leituraInicialNovo,
        resultadoFinal = runCatching { ResultadoFinal.valueOf(resultadoFinal) }.getOrDefault(ResultadoFinal.PENDENTE),
        vazoes = vazoes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    private fun Ensaio.toEntity() = EnsaioEntity(
        id = id,
        hidrometroModeloId = hidrometroModeloId,
        numeroHidrometro = numeroHidrometro,
        cliente = cliente,
        matricula = matricula,
        endereco = endereco,
        cidade = cidade,
        bairro = bairro,
        dataEnsaio = dataEnsaio,
        tecnicoResponsavel = tecnicoResponsavel,
        idadeHidrometro = idadeHidrometro,
        temperaturaAgua = temperaturaAgua,
        observacoes = observacoes,
        nomeCompanhia = nomeCompanhia,
        norma = norma.name,
        metodoEnsaio = metodoEnsaio.name,
        maletaNome = maletaNome,
        erroPadrao = erroPadrao,
        pressaoMedia = pressaoMedia,
        realizado = realizado,
        motivoNaoRealizado = motivoNaoRealizado,
        leituraFinalReprovado = leituraFinalReprovado,
        numeroSerieNovo = numeroSerieNovo,
        leituraInicialNovo = leituraInicialNovo,
        resultadoFinal = resultadoFinal.name
    )

    private fun VazaoEnsaioEntity.toDomain() = VazaoEnsaio(
        id = id,
        tipoVazao = TipoVazao.valueOf(tipoVazao),
        m1Escoamento = m1Escoamento,
        m1LeituraInicial = m1LeituraInicial,
        m1LeituraFinal = m1LeituraFinal,
        m2Escoamento = m2Escoamento,
        m2LeituraInicial = m2LeituraInicial,
        m2LeituraFinal = m2LeituraFinal,
        m3Escoamento = m3Escoamento,
        m3LeituraInicial = m3LeituraInicial,
        m3LeituraFinal = m3LeituraFinal,
        m1PadraoInicial = m1PadraoInicial,
        m1PadraoFinal = m1PadraoFinal,
        m2PadraoInicial = m2PadraoInicial,
        m2PadraoFinal = m2PadraoFinal,
        m3PadraoInicial = m3PadraoInicial,
        m3PadraoFinal = m3PadraoFinal,
        erro1 = erro1,
        erro2 = erro2,
        erro3 = erro3,
        erroMedio = erroMedio,
        aprovado = aprovado
    )

    private fun VazaoEnsaio.toEntity(ensaioId: Long) = VazaoEnsaioEntity(
        id = id,
        ensaioId = ensaioId,
        tipoVazao = tipoVazao.name,
        m1Escoamento = m1Escoamento,
        m1LeituraInicial = m1LeituraInicial,
        m1LeituraFinal = m1LeituraFinal,
        m2Escoamento = m2Escoamento,
        m2LeituraInicial = m2LeituraInicial,
        m2LeituraFinal = m2LeituraFinal,
        m3Escoamento = m3Escoamento,
        m3LeituraInicial = m3LeituraInicial,
        m3LeituraFinal = m3LeituraFinal,
        m1PadraoInicial = m1PadraoInicial,
        m1PadraoFinal = m1PadraoFinal,
        m2PadraoInicial = m2PadraoInicial,
        m2PadraoFinal = m2PadraoFinal,
        m3PadraoInicial = m3PadraoInicial,
        m3PadraoFinal = m3PadraoFinal,
        erro1 = erro1,
        erro2 = erro2,
        erro3 = erro3,
        erroMedio = erroMedio,
        aprovado = aprovado
    )
}
