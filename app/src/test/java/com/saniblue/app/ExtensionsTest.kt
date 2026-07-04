package com.saniblue.app

import com.saniblue.app.domain.model.NormaEnsaio
import com.saniblue.app.util.filtrarDecimal
import com.saniblue.app.util.filtrarSerialHidrometro
import com.saniblue.app.util.isLetraCapacidadeConhecida
import com.saniblue.app.util.isSerialHidrometroValido
import com.saniblue.app.util.normaDoSerial
import com.saniblue.app.util.toDoubleLocale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExtensionsTest {

    // ── Máscara do nº de série — formato adaptativo: 10 car. (Portaria 246:
    // Letra+2nº+Letra+6nº) ou 12 car. (Portaria 155: Letra+2nº+2Letras+7nº),
    // decidido pelo 5º caractere (dígito → 246; letra → 155) ──

    @Test
    fun `serial 246 completo e valido passa na mascara sem alteracao`() {
        assertEquals("Y20B123456", "Y20B123456".filtrarSerialHidrometro())
    }

    @Test
    fun `serial 155 completo e valido passa na mascara sem alteracao`() {
        assertEquals("Z25AK0314293", "Z25AK0314293".filtrarSerialHidrometro())
    }

    @Test
    fun `mascara converte minusculas para maiusculas`() {
        assertEquals("Y20B123456", "y20b123456".filtrarSerialHidrometro())
        assertEquals("Z25AK0314293", "z25ak0314293".filtrarSerialHidrometro())
    }

    @Test
    fun `mascara ignora caracteres na posicao errada`() {
        // dígito na posição 0 (deveria ser letra) é descartado; letras onde deveriam ser dígitos também
        assertEquals("A99A999999", "1A9x9A999999".filtrarSerialHidrometro())
    }

    @Test
    fun `mascara corta em 10 caracteres quando o 5o caractere e digito (246)`() {
        assertEquals("A12B345678", "A12B34567890123".filtrarSerialHidrometro())
    }

    @Test
    fun `mascara corta em 12 caracteres quando o 5o caractere e letra (155)`() {
        assertEquals("Z25AK0314293", "Z25AK031429399999".filtrarSerialHidrometro())
    }

    @Test
    fun `mascara aceita entrada parcial durante a digitacao`() {
        assertEquals("A12", "A12".filtrarSerialHidrometro())
    }

    @Test
    fun `validacao aceita os dois formatos completos`() {
        assertTrue("A99A999999".isSerialHidrometroValido())      // 246 — 10 car.
        assertTrue("Y20B123456".isSerialHidrometroValido())      // 246 — 10 car.
        assertTrue("Z25AK0314293".isSerialHidrometroValido())    // 155 — 12 car.
        assertFalse("A99A99999".isSerialHidrometroValido())      // 9 caracteres
        assertFalse("A99A9999999".isSerialHidrometroValido())    // 11 caracteres (nem 10 nem 12)
        assertFalse("999A999999".isSerialHidrometroValido())     // começa com número
        assertFalse("A9AA999999".isSerialHidrometroValido())     // letra onde deveria ser número
        assertFalse("".isSerialHidrometroValido())
    }

    // ── Detecção da norma pelo formato do serial ──

    @Test
    fun `normaDoSerial detecta 246 pelo tamanho de 10 caracteres`() {
        assertEquals(NormaEnsaio.PORTARIA_246, "Y20B123456".normaDoSerial())
    }

    @Test
    fun `normaDoSerial detecta 155 pelo tamanho de 12 caracteres`() {
        assertEquals(NormaEnsaio.PORTARIA_155, "Z25AK0314293".normaDoSerial())
    }

    @Test
    fun `normaDoSerial retorna null para serial incompleto ou invalido`() {
        assertNull("Y20B".normaDoSerial())
        assertNull("A99A9999999".normaDoSerial())
    }

    // ── Letra de capacidade conhecida (catálogo cadastrado) ──

    @Test
    fun `letras conhecidas da portaria 246 sao Y e A`() {
        assertTrue('Y'.isLetraCapacidadeConhecida(NormaEnsaio.PORTARIA_246))
        assertTrue('A'.isLetraCapacidadeConhecida(NormaEnsaio.PORTARIA_246))
        assertFalse('Z'.isLetraCapacidadeConhecida(NormaEnsaio.PORTARIA_246))
        assertFalse('B'.isLetraCapacidadeConhecida(NormaEnsaio.PORTARIA_246))
    }

    @Test
    fun `letras conhecidas da portaria 155 sao Y Z e A`() {
        assertTrue('Y'.isLetraCapacidadeConhecida(NormaEnsaio.PORTARIA_155))
        assertTrue('Z'.isLetraCapacidadeConhecida(NormaEnsaio.PORTARIA_155))
        assertTrue('A'.isLetraCapacidadeConhecida(NormaEnsaio.PORTARIA_155))
        assertFalse('B'.isLetraCapacidadeConhecida(NormaEnsaio.PORTARIA_155))
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
