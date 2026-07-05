package com.saniblue.app.domain.session

import com.saniblue.app.BuildConfig
import com.saniblue.app.domain.model.Maleta
import com.saniblue.app.domain.model.MetodoEnsaio
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Configuração do app, definida no build de cada maleta (não mais escolhida no login):
 *  - método de ensaio (escoamento direto / comparativo por leitura) — travado pelo flavor
 *  - maleta em uso e seu erro padrão — embutidos via -PmaletaNome / -PerroPadrao
 *
 * Cada maleta vendida recebe um APK próprio com esses valores fixos, então o técnico
 * não escolhe nem pode errar o tipo ou o erro padrão. Os valores vêm do BuildConfig
 * (ver productFlavors + buildConfigField em app/build.gradle.kts).
 */
@Singleton
class SessaoTecnico @Inject constructor() {

    val metodoEnsaio: MetodoEnsaio =
        runCatching { MetodoEnsaio.valueOf(BuildConfig.TIPO_ENSAIO) }
            .getOrDefault(MetodoEnsaio.ESCOAMENTO_DIRETO)

    val maleta: Maleta = Maleta(
        id = "build",
        nome = BuildConfig.MALETA_NOME,
        erroPadrao = BuildConfig.ERRO_PADRAO
    )
}
