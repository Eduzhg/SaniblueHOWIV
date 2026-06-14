package com.saniblue.app

import com.saniblue.app.domain.usecase.CalcularIdadeHidrometroUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class CalcularIdadeHidrometroUseCaseTest {

    private lateinit var useCase: CalcularIdadeHidrometroUseCase

    @Before
    fun setup() {
        useCase = CalcularIdadeHidrometroUseCase()
    }

    @Test
    fun `extrai ano de fabricacao dos 2 primeiros digitos`() {
        // Y20B123456 → fab. 2020; em 2026 → 6 anos
        val resultado = useCase("Y20B123456", anoAtual = 2026)
        assertEquals("6 ano(s) — fab. 2020", resultado)
    }

    @Test
    fun `ano futuro assume seculo anterior`() {
        // 98 → 2098 seria futuro em 2026, então vira 1998
        val resultado = useCase("A98B000000", anoAtual = 2026)
        assertEquals("28 ano(s) — fab. 1998", resultado)
    }

    @Test
    fun `idade zero no proprio ano de fabricacao`() {
        val resultado = useCase("Z26A111111", anoAtual = 2026)
        assertEquals("0 ano(s) — fab. 2026", resultado)
    }

    @Test
    fun `retorna null quando nao ha dois digitos`() {
        assertNull(useCase("ABCDEF", anoAtual = 2026))
    }
}
