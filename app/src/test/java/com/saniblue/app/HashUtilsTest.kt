package com.saniblue.app

import com.saniblue.app.util.HashUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class HashUtilsTest {

    @Test
    fun `sha256 gera hash consistente`() {
        val hash1 = HashUtils.sha256("admin123")
        val hash2 = HashUtils.sha256("admin123")
        assertEquals(hash1, hash2)
    }

    @Test
    fun `sha256 senhas diferentes geram hashes diferentes`() {
        val hash1 = HashUtils.sha256("admin123")
        val hash2 = HashUtils.sha256("tecnico123")
        assertNotEquals(hash1, hash2)
    }

    @Test
    fun `sha256 retorna string hexadecimal de 64 caracteres`() {
        val hash = HashUtils.sha256("qualquercoisa")
        assertEquals(64, hash.length)
        assertTrue(hash.all { it in '0'..'9' || it in 'a'..'f' })
    }

    private fun assertTrue(value: Boolean) {
        assertEquals(true, value)
    }
}
