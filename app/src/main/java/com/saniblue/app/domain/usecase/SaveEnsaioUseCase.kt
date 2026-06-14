package com.saniblue.app.domain.usecase

import com.saniblue.app.domain.model.Ensaio
import com.saniblue.app.domain.model.ResultadoFinal
import com.saniblue.app.domain.repository.EnsaioRepository
import javax.inject.Inject

class SaveEnsaioUseCase @Inject constructor(
    private val repository: EnsaioRepository,
    private val calcularErro: CalcularErroUseCase
) {
    suspend operator fun invoke(ensaio: Ensaio): Result<Long> {
        return try {
            // Ensaio não realizado: não há medições a calcular
            if (!ensaio.realizado) {
                val id = repository.save(
                    ensaio.copy(
                        vazoes = emptyList(),
                        resultadoFinal = ResultadoFinal.NAO_REALIZADO
                    )
                )
                return Result.success(id)
            }

            // Recalcular todos os erros antes de salvar (limites vêm da norma,
            // escoamento corrigido pelo erro padrão da maleta)
            val vazoesCalculadas = ensaio.vazoes.map { vazao ->
                calcularErro.calcularVazao(vazao, ensaio.norma, ensaio.erroPadrao)
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
