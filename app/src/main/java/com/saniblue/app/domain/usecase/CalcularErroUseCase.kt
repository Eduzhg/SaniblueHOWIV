package com.saniblue.app.domain.usecase

import com.saniblue.app.domain.model.NormaEnsaio
import com.saniblue.app.domain.model.ResultadoFinal
import com.saniblue.app.domain.model.TipoVazao
import com.saniblue.app.domain.model.VazaoEnsaio
import javax.inject.Inject

class CalcularErroUseCase @Inject constructor() {

    /**
     * Calcula o erro percentual de uma medição.
     * Erro = ((Totalizado - Escoamento) / Escoamento) * 100
     * Totalizado = Leitura Final - Leitura Inicial
     */
    fun calcularErro(escoamento: Double, leituraInicial: Double, leituraFinal: Double): Double {
        if (escoamento == 0.0) return 0.0
        val totalizado = leituraFinal - leituraInicial
        return ((totalizado - escoamento) / escoamento) * 100.0
    }

    /**
     * Calcula e retorna VazaoEnsaio com todos os erros preenchidos.
     * Requer as 3 medições com escoamento > 0 para marcar como aprovado.
     * Os limites de aprovação vêm da NORMA selecionada.
     */
    fun calcularVazao(vazao: VazaoEnsaio, norma: NormaEnsaio): VazaoEnsaio {
        // Somente calcula erro em medições com escoamento real (> 0)
        val m1valida = vazao.m1Escoamento > 0
        val m2valida = vazao.m2Escoamento > 0
        val m3valida = vazao.m3Escoamento > 0

        val erro1 = if (m1valida) calcularErro(vazao.m1Escoamento, vazao.m1LeituraInicial, vazao.m1LeituraFinal) else 0.0
        val erro2 = if (m2valida) calcularErro(vazao.m2Escoamento, vazao.m2LeituraInicial, vazao.m2LeituraFinal) else 0.0
        val erro3 = if (m3valida) calcularErro(vazao.m3Escoamento, vazao.m3LeituraInicial, vazao.m3LeituraFinal) else 0.0

        // Exige as 3 medições preenchidas para calcular média e aprovar
        val todasValidas = m1valida && m2valida && m3valida
        val erroMedio = if (todasValidas) (erro1 + erro2 + erro3) / 3.0 else 0.0
        val aprovado = todasValidas && isVazaoAprovada(erroMedio, vazao.tipoVazao, norma)

        return vazao.copy(
            erro1 = erro1,
            erro2 = erro2,
            erro3 = erro3,
            erroMedio = erroMedio,
            aprovado = aprovado
        )
    }

    fun isVazaoAprovada(erroMedio: Double, tipoVazao: TipoVazao, norma: NormaEnsaio): Boolean =
        erroMedio >= norma.limiteMin(tipoVazao) && erroMedio <= norma.limiteMax(tipoVazao)

    fun calcularResultadoFinal(vazoes: List<VazaoEnsaio>): ResultadoFinal {
        if (vazoes.isEmpty()) return ResultadoFinal.PENDENTE
        val todasVazoes = listOf(TipoVazao.NOMINAL, TipoVazao.TRANSICAO, TipoVazao.MINIMA)
        val tiposPresentes = vazoes.map { it.tipoVazao }.toSet()
        if (!tiposPresentes.containsAll(todasVazoes)) return ResultadoFinal.PENDENTE
        return if (vazoes.all { it.aprovado }) ResultadoFinal.APROVADO else ResultadoFinal.REPROVADO
    }

    fun formatarErro(erro: Double): String = "%.2f%%".format(erro)

    fun getLimitesLabel(tipoVazao: TipoVazao, norma: NormaEnsaio): String =
        norma.limiteLabel(tipoVazao)
}
