package com.saniblue.app.domain.session

import com.saniblue.app.domain.model.Maleta
import com.saniblue.app.domain.model.MaletasDisponiveis
import com.saniblue.app.domain.model.MetodoEnsaio
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Guarda as escolhas feitas pelo técnico no login e válidas para toda a sessão:
 *  - método de ensaio (escoamento direto / comparativo por leitura)
 *  - maleta em uso (define o erro padrão aplicado no cálculo)
 *
 * É um singleton em memória — reiniciar o app exige novo login, então a seleção
 * é refeita por turno, que é exatamente o comportamento desejado.
 */
@Singleton
class SessaoTecnico @Inject constructor() {
    var metodoEnsaio: MetodoEnsaio = MetodoEnsaio.ESCOAMENTO_DIRETO
        private set

    var maleta: Maleta = MaletasDisponiveis.padrao
        private set

    fun definir(metodo: MetodoEnsaio, maleta: Maleta) {
        this.metodoEnsaio = metodo
        this.maleta = maleta
    }
}
