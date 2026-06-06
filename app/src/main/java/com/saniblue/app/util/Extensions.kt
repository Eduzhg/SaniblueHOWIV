package com.saniblue.app.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Long.toDataFormatada(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
    return sdf.format(Date(this))
}

fun Double.formatarErro(): String = "%.2f%%".format(this)

fun Double.formatarLitragem(): String = "%.3f L".format(this)

/**
 * Aceita tanto ponto quanto vírgula como separador decimal (padrão BR).
 * Ex.: "50,372" e "50.372" ambos resultam em 50.372
 */
fun String.toDoubleLocale(): Double? = trim().replace(",", ".").toDoubleOrNull()

fun String.toDoubleOrZero(): Double = toDoubleLocale() ?: 0.0

fun dataAtualFormatada(): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR"))
    return sdf.format(Date())
}
