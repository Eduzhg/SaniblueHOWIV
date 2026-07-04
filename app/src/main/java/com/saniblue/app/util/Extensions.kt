package com.saniblue.app.util

import com.saniblue.app.domain.model.NormaEnsaio
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

private val REGEX_SERIAL_246 = Regex("^[A-Z][0-9]{2}[A-Z][0-9]{6}$")
private val REGEX_SERIAL_155 = Regex("^[A-Z][0-9]{2}[A-Z]{2}[0-9]{7}$")

/**
 * Máscara adaptativa do número de série do hidrômetro — dois formatos possíveis,
 * diferenciados pelo 5º caractere (posição 4):
 *  - Portaria 246 (10 caracteres): Letra, 2 números (ano), Letra, 6 números.
 *    Ex.: Y20B123456 — o 5º caractere é dígito.
 *  - Portaria 155 (12 caracteres): Letra, 2 números (ano), 2 Letras, 7 números.
 *    Ex.: Z25AK0314293 — o 5º caractere é letra.
 * A 1ª letra identifica a capacidade do hidrômetro (ver HidrometroModelo); as
 * demais letras são só o código do fabricante e não entram em nenhum cálculo.
 * Letras são convertidas para maiúsculas e caracteres fora do padrão são ignorados.
 */
fun String.filtrarSerialHidrometro(): String {
    val sb = StringBuilder()
    var formato: Int? = null // 10 (246) ou 12 (155) — definido pelo 5º caractere digitado
    for (c in this.uppercase()) {
        val pos = sb.length
        if (formato != null && pos >= formato) break
        val valido = when (pos) {
            0, 3 -> c.isLetter()
            4 -> when {
                c.isLetter() -> { formato = 12; true }
                c.isDigit() -> { formato = 10; true }
                else -> false
            }
            else -> c.isDigit()
        }
        if (valido) sb.append(c)
    }
    return sb.toString()
}

/** Nº de série completo e em formato válido (Portaria 246 de 10 ou Portaria 155 de 12 caracteres). */
fun String.isSerialHidrometroValido(): Boolean =
    REGEX_SERIAL_246.matches(this) || REGEX_SERIAL_155.matches(this)

/** Norma detectada pelo formato (tamanho) do nº de série; null se incompleto ou inválido. */
fun String.normaDoSerial(): NormaEnsaio? = when {
    REGEX_SERIAL_246.matches(this) -> NormaEnsaio.PORTARIA_246
    REGEX_SERIAL_155.matches(this) -> NormaEnsaio.PORTARIA_155
    else -> null
}

private val LETRAS_CONHECIDAS_246 = setOf('Y', 'A')
private val LETRAS_CONHECIDAS_155 = setOf('Y', 'Z', 'A')

/** Se a letra de capacidade (1º caractere do serial) tem catálogo cadastrado para a norma. */
fun Char.isLetraCapacidadeConhecida(norma: NormaEnsaio): Boolean = when (norma) {
    NormaEnsaio.PORTARIA_246 -> this in LETRAS_CONHECIDAS_246
    NormaEnsaio.PORTARIA_155 -> this in LETRAS_CONHECIDAS_155
}

fun dataAtualFormatada(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return sdf.format(Date())
}
