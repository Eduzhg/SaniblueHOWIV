package com.saniblue.app

import com.saniblue.app.util.filtrarDecimal
import com.saniblue.app.util.filtrarSerialHidrometro
import com.saniblue.app.util.isSerialHidrometroValido
import com.saniblue.app.util.toDoubleLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionsTest {

    // ── Máscara do nº de série: Letra, 2 números, Letra, 6 números ──

    @Test
    fun `serial completo valido passa na mascara sem alteracao`() {
        assertEquals("Y20B123456", "Y20B123456".filtrarSerialHidrometro())
    }

    @Test
    fun `mascara converte minusculas para maiusculas`() {
        assertEquals("Y20B123456", "y20b123456".filtrarSerialHidrometro())
    }

    @Test
    fun `mascara ignora caracteres na posicao errada`() {
        // dígito na posição 0 (deveria ser letra) é descartado; letras onde deveriam ser dígitos também
        assertEquals("A99A999999", "1A9x9A999999".filtrarSerialHidrometro())
    }

    @Test
    fun `mascara corta o que passar de 10 caracteres`() {
        assertEquals("A12B345678", "A12B34567890123".filtrarSerialHidrometro())
    }

    @Test
    fun `mascara aceita entrada parcial durante a digitacao`() {
        assertEquals("A12", "A12".filtrarSerialHidrometro())
    }

    @Test
    fun `validacao aceita apenas o formato completo`() {
        assertTrue("A99A999999".isSerialHidrometroValido())
        assertTrue("Y20B123456".isSerialHidrometroValido())
        assertFalse("A99A99999".isSerialHidrometroValido())   // 9 caracteres
        assertFalse("A99A9999999".isSerialHidrometroValido()) // 11 caracteres
        assertFalse("999A999999".isSerialHidrometroValido())  // começa com número
        assertFalse("A9AA999999".isSerialHidrometroValido())  // letra onde deveria ser número
        assertFalse("".isSerialHidrometroValido())
    }

    // ── Filtro decimal (medições usam 3 casas) ──

    @Test
    fun `filtro decimal com 3 casas mantem ate 3 digitos apos separador`() {
        assertEquals("10,372", "10,372".filtrarDecimal(3))
        assertEquals("10.372", "10.3729".filtrarDecimal(3))
    }

    @Test
    fun `filtro decimal padrao mantem 2 casas`() {
        assertEquals("10,37", "10,372".filtrarDecimal())
    }

    @Test
    fun `filtro decimal exige digito antes do separador e um separador so`() {
        assertEquals("", ",".filtrarDecimal(3))
        assertEquals("1,23", "1,2,3".filtrarDecimal(3))
    }

    // ── Conversão local (vírgula ou ponto) ──

    @Test
    fun `toDoubleLocale aceita virgula e ponto`() {
        assertEquals(50.372, "50,372".toDoubleLocale()!!, 1e-9)
        assertEquals(50.372, "50.372".toDoubleLocale()!!, 1e-9)
        assertNull("abc".toDoubleLocale())
    }
}
