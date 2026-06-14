package com.saniblue.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Aceita tanto ponto quanto vírgula como separador decimal (padrão BR).
 * Ex.: "50,372" e "50.372" ambos resultam em 50.372
 */
fun String.toDoubleLocale(): Double? = trim().replace(",", ".").toDoubleOrNull()

/**
 * Filtra a entrada para um número decimal com no máximo `maxDecimais` casas.
 * Mantém o separador digitado (vírgula ou ponto) e exige ao menos um dígito antes.
 */
fun String.filtrarDecimal(maxDecimais: Int = 2): String {
    val sb = StringBuilder()
    var temSeparador = false
    var casas = 0
    for (c in this) {
        when {
            c.isDigit() -> {
                if (temSeparador) {
                    if (casas < maxDecimais) { sb.append(c); casas++ }
                } else sb.append(c)
            }
            (c == ',' || c == '.') && !temSeparador && sb.isNotEmpty() -> {
                temSeparador = true
                sb.append(c)
            }
        }
    }
    return sb.toString()
}

/**
 * Máscara do número de série do hidrômetro.
 * Formato fixo (10 caracteres): Letra, 2 números, Letra, 6 números. Ex.: Y20B123456
 * Letras são convertidas para maiúsculas e caracteres fora do padrão são ignorados.
 */
fun String.filtrarSerialHidrometro(): String {
    val sb = StringBuilder()
    for (c in this.uppercase()) {
        val pos = sb.length
        if (pos >= 10) break
        val valido = when (pos) {
            0, 3 -> c.isLetter()
            else -> c.isDigit()
        }
        if (valido) sb.append(c)
    }
    return sb.toString()
}

/** Verifica se o nº de série está completo no formato Letra-NN-Letra-NNNNNN. */
fun String.isSerialHidrometroValido(): Boolean =
    Regex("^[A-Z][0-9]{2}[A-Z][0-9]{6}$").matches(this)

fun dataAtualFormatada(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return sdf.format(Date())
}
