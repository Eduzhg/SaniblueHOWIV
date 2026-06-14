package com.saniblue.app.domain.usecase

import java.util.Calendar
import javax.inject.Inject

/**
 * Calcula a idade do hidrômetro a partir do número de série.
 *
 * Convenção: os 2 primeiros dígitos do número de série representam o ano de
 * fabricação (ex.: "Y20B123456" → 2020). A idade é `ano atual − ano de fabricação`.
 * Se o ano resultante for futuro, assume o século anterior (ex.: 98 → 1998).
 *
 * `anoAtual` é parâmetro (com default = ano corrente) para permitir testes determinísticos.
 */
class CalcularIdadeHidrometroUseCase @Inject constructor() {

    operator fun invoke(
        numeroSerie: String,
        anoAtual: Int = Calendar.getInstance().get(Calendar.YEAR)
    ): String? {
        val yy = Regex("(\\d{2})").find(numeroSerie)?.groupValues?.get(1)?.toIntOrNull() ?: return null
        var anoFabricacao = 2000 + yy
        if (anoFabricacao > anoAtual) anoFabricacao = 1900 + yy
        val idade = anoAtual - anoFabricacao
        if (idade < 0) return null
        return "$idade ano(s) — fab. $anoFabricacao"
    }
}
