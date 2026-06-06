package com.saniblue.app

import com.saniblue.app.domain.model.NormaEnsaio
import com.saniblue.app.domain.model.ResultadoFinal
import com.saniblue.app.domain.model.TipoVazao
import com.saniblue.app.domain.model.VazaoEnsaio
import com.saniblue.app.domain.usecase.CalcularErroUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalcularErroUseCaseTest {

    private lateinit var useCase: CalcularErroUseCase

    // Portaria 246: ±5% (QN/QT) e ±10% (QM)
    private val norma246 = NormaEnsaio.PORTARIA_246

    @Before
    fun setup() {
        useCase = CalcularErroUseCase()
    }

    // === Cálculo de erro ===

    @Test
    fun `calcula erro zero quando totalizado igual escoamento`() {
        // Escoamento = 10L, LI = 0, LF = 10 => Totalizado = 10
        val erro = useCase.calcularErro(10.0, 0.0, 10.0)
        assertEquals(0.0, erro, 0.001)
    }

    @Test
    fun `calcula erro positivo quando totalizado maior que escoamento`() {
        // Escoamento = 10L, LI = 0, LF = 10.5 => Totalizado = 10.5 => Erro = +5%
        val erro = useCase.calcularErro(10.0, 0.0, 10.5)
        assertEquals(5.0, erro, 0.001)
    }

    @Test
    fun `calcula erro negativo quando totalizado menor que escoamento`() {
        // Escoamento = 10L, LI = 0, LF = 9.5 => Totalizado = 9.5 => Erro = -5%
        val erro = useCase.calcularErro(10.0, 0.0, 9.5)
        assertEquals(-5.0, erro, 0.001)
    }

    @Test
    fun `retorna zero quando escoamento e zero`() {
        val erro = useCase.calcularErro(0.0, 0.0, 10.0)
        assertEquals(0.0, erro, 0.001)
    }

    @Test
    fun `calcula com leitura inicial diferente de zero`() {
        // Escoamento = 100L, LI = 1000.0, LF = 1100.0 => Totalizado = 100 => Erro = 0%
        val erro = useCase.calcularErro(100.0, 1000.0, 1100.0)
        assertEquals(0.0, erro, 0.001)
    }

    // === Aprovação por vazão ===

    @Test
    fun `aprova nominal dentro do limite 5 por cento`() {
        assertTrue(useCase.isVazaoAprovada(3.5, TipoVazao.NOMINAL, norma246))
        assertTrue(useCase.isVazaoAprovada(-4.9, TipoVazao.NOMINAL, norma246))
        assertTrue(useCase.isVazaoAprovada(0.0, TipoVazao.NOMINAL, norma246))
    }

    @Test
    fun `reprova nominal fora do limite 5 por cento`() {
        assertFalse(useCase.isVazaoAprovada(5.1, TipoVazao.NOMINAL, norma246))
        assertFalse(useCase.isVazaoAprovada(-5.1, TipoVazao.NOMINAL, norma246))
    }

    @Test
    fun `aprova minima dentro do limite 10 por cento`() {
        assertTrue(useCase.isVazaoAprovada(9.9, TipoVazao.MINIMA, norma246))
        assertTrue(useCase.isVazaoAprovada(-10.0, TipoVazao.MINIMA, norma246))
    }

    @Test
    fun `reprova minima fora do limite 10 por cento`() {
        assertFalse(useCase.isVazaoAprovada(10.1, TipoVazao.MINIMA, norma246))
        assertFalse(useCase.isVazaoAprovada(-10.1, TipoVazao.MINIMA, norma246))
    }

    // === Resultado final ===

    @Test
    fun `resultado final aprovado quando todas as vazoes aprovadas`() {
        val vazoes = listOf(
            VazaoEnsaio(tipoVazao = TipoVazao.NOMINAL, aprovado = true),
            VazaoEnsaio(tipoVazao = TipoVazao.TRANSICAO, aprovado = true),
            VazaoEnsaio(tipoVazao = TipoVazao.MINIMA, aprovado = true)
        )
        assertEquals(ResultadoFinal.APROVADO, useCase.calcularResultadoFinal(vazoes))
    }

    @Test
    fun `resultado final reprovado quando qualquer vazao reprovada`() {
        val vazoes = listOf(
            VazaoEnsaio(tipoVazao = TipoVazao.NOMINAL, aprovado = true),
            VazaoEnsaio(tipoVazao = TipoVazao.TRANSICAO, aprovado = false),
            VazaoEnsaio(tipoVazao = TipoVazao.MINIMA, aprovado = true)
        )
        assertEquals(ResultadoFinal.REPROVADO, useCase.calcularResultadoFinal(vazoes))
    }

    @Test
    fun `resultado pendente quando lista vazia`() {
        assertEquals(ResultadoFinal.PENDENTE, useCase.calcularResultadoFinal(emptyList()))
    }

    @Test
    fun `resultado pendente quando falta algum tipo de vazao`() {
        val vazoes = listOf(
            VazaoEnsaio(tipoVazao = TipoVazao.NOMINAL, aprovado = true)
        )
        assertEquals(ResultadoFinal.PENDENTE, useCase.calcularResultadoFinal(vazoes))
    }

    // === Cálculo completo de VazaoEnsaio ===

    @Test
    fun `calcula vazao completo com tres medicoes`() {
        val vazao = VazaoEnsaio(
            tipoVazao = TipoVazao.NOMINAL,
            m1Escoamento = 10.0, m1LeituraInicial = 0.0, m1LeituraFinal = 10.0,
            m2Escoamento = 10.0, m2LeituraInicial = 10.0, m2LeituraFinal = 20.1,
            m3Escoamento = 10.0, m3LeituraInicial = 20.1, m3LeituraFinal = 30.2
        )
        val resultado = useCase.calcularVazao(vazao, norma246)
        assertEquals(0.0, resultado.erro1, 0.001)
        assertEquals(1.0, resultado.erro2, 0.001)
        assertEquals(1.0, resultado.erro3, 0.001)
        assertEquals(0.667, resultado.erroMedio, 0.01)
        assertTrue(resultado.aprovado)
    }

    @Test
    fun `nao aprova vazao com medicoes zeradas (sem dados)`() {
        // Sem dados → escoamento = 0 → NÃO deve aprovar
        val vazaoSemDados = VazaoEnsaio(
            tipoVazao = TipoVazao.NOMINAL,
            m1Escoamento = 0.0, m1LeituraInicial = 0.0, m1LeituraFinal = 0.0,
            m2Escoamento = 0.0, m2LeituraInicial = 0.0, m2LeituraFinal = 0.0,
            m3Escoamento = 0.0, m3LeituraInicial = 0.0, m3LeituraFinal = 0.0
        )
        val resultado = useCase.calcularVazao(vazaoSemDados, norma246)
        assertFalse("Vazão sem dados deve ser REPROVADA", resultado.aprovado)
    }

    @Test
    fun `nao aprova quando apenas uma medicao tem dados`() {
        val vazaoParcial = VazaoEnsaio(
            tipoVazao = TipoVazao.NOMINAL,
            m1Escoamento = 10.0, m1LeituraInicial = 0.0, m1LeituraFinal = 10.0,
            m2Escoamento = 0.0, m2LeituraInicial = 0.0, m2LeituraFinal = 0.0,
            m3Escoamento = 0.0, m3LeituraInicial = 0.0, m3LeituraFinal = 0.0
        )
        val resultado = useCase.calcularVazao(vazaoParcial, norma246)
        assertFalse("Vazão com dados parciais deve ser REPROVADA", resultado.aprovado)
    }

    @Test
    fun `valida exemplo real do usuario`() {
        // Escoamento: 50.372 | LI: 713.10 | LF: 761.84
        // Totalizado = 48.74 | Erro esperado = -3.24%
        val erro = useCase.calcularErro(50.372, 713.10, 761.84)
        assertEquals(-3.24, erro, 0.01)
    }
}

class ToDoubleLocaleTest {
    @Test
    fun `aceita ponto como separador decimal`() {
        assertEquals(50.372, "50.372".toDoubleLocaleTest()!!, 0.0001)
    }

    @Test
    fun `aceita virgula como separador decimal`() {
        assertEquals(50.372, "50,372".toDoubleLocaleTest()!!, 0.0001)
    }

    @Test
    fun `aceita inteiro sem separador`() {
        assertEquals(713.0, "713".toDoubleLocaleTest()!!, 0.0001)
    }

    @Test
    fun `retorna null para string vazia`() {
        assertNull("".toDoubleLocaleTest())
    }

    @Test
    fun `retorna null para texto invalido`() {
        assertNull("abc".toDoubleLocaleTest())
    }

    private fun String.toDoubleLocaleTest(): Double? =
        trim().replace(",", ".").toDoubleOrNull()

    private fun assertNull(value: Double?) {
        assertEquals(null, value)
    }
}
