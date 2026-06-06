package com.saniblue.app.domain.usecase

import com.saniblue.app.domain.model.Ensaio
import com.saniblue.app.domain.model.HidrometroModelo
import com.saniblue.app.domain.repository.EnsaioRepository
import javax.inject.Inject

class SaveEnsaioUseCase @Inject constructor(
    private val repository: EnsaioRepository,
    private val calcularErro: CalcularErroUseCase
) {
    suspend operator fun invoke(ensaio: Ensaio, modelo: HidrometroModelo): Result<Long> {
        return try {
            // Recalcular todos os erros antes de salvar (limites vêm da norma)
            val vazoesCalculadas = ensaio.vazoes.map { vazao ->
                calcularErro.calcularVazao(vazao, ensaio.norma)
            }
            val resultadoFinal = calcularErro.calcularResultadoFinal(vazoesCalculadas)

            val ensaioFinal = ensaio.copy(
                vazoes = vazoesCalculadas,
                resultadoFinal = resultadoFinal
            )
            val id = repository.save(ensaioFinal)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
