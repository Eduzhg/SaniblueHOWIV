package com.saniblue.app.data.local.datastore

import org.json.JSONObject

/**
 * Snapshot dos campos editáveis de um ensaio em andamento (rascunho).
 * É um DTO de persistência — guarda os valores como o usuário digitou (texto),
 * sem regra de negócio. A serialização JSON fica em [RascunhoEnsaioSerializer].
 */
data class RascunhoEnsaio(
    val numeroHidrometro: String = "",
    val cliente: String = "",
    val nomeCompanhia: String = "",
    val matricula: String = "",
    val endereco: String = "",
    val cidade: String = "",
    val bairro: String = "",
    val dataEnsaio: String = "",
    val tecnicoResponsavel: String = "",
    val idadeHidrometro: String = "",
    val pressaoMedia: String = "",
    val observacoes: String = "",
    val norma: String = "",
    val modeloSelecionadoId: Long = 0L,
    val realizado: Boolean = true,
    val motivoNaoRealizado: String = "",
    val leituraFinalReprovado: String = "",
    val numeroSerieNovo: String = "",
    val leituraInicialNovo: String = "",
    val clienteAcompanhou: Boolean = false,
    val clienteRecusouDados: Boolean = false,
    val acompanhanteNome: String = "",
    val acompanhanteDocumento: String = "",
    val acompanhanteTelefone: String = "",
    val nominal: RascunhoVazao = RascunhoVazao(),
    val transicao: RascunhoVazao = RascunhoVazao(),
    val minima: RascunhoVazao = RascunhoVazao()
)

data class RascunhoVazao(
    val m1: RascunhoMedicao = RascunhoMedicao(),
    val m2: RascunhoMedicao = RascunhoMedicao(),
    val m3: RascunhoMedicao = RascunhoMedicao()
)

data class RascunhoMedicao(
    val escoamento: String = "",
    val leituraInicial: String = "",
    val leituraFinal: String = "",
    val padraoInicial: String = "",
    val padraoFinal: String = ""
)

/** Converte [RascunhoEnsaio] de/para JSON. Único ponto que conhece o formato. */
object RascunhoEnsaioSerializer {

    fun toJson(r: RascunhoEnsaio): String = JSONObject().apply {
        put("numeroHidrometro", r.numeroHidrometro)
        put("cliente", r.cliente)
        put("nomeCompanhia", r.nomeCompanhia)
        put("matricula", r.matricula)
        put("endereco", r.endereco)
        put("cidade", r.cidade)
        put("bairro", r.bairro)
        put("dataEnsaio", r.dataEnsaio)
        put("tecnico", r.tecnicoResponsavel)
        put("idade", r.idadeHidrometro)
        put("pressao", r.pressaoMedia)
        put("observacoes", r.observacoes)
        put("norma", r.norma)
        put("modeloId", r.modeloSelecionadoId)
        put("realizado", r.realizado)
        put("motivo", r.motivoNaoRealizado)
        put("lfReprovado", r.leituraFinalReprovado)
        put("serieNovo", r.numeroSerieNovo)
        put("liNovo", r.leituraInicialNovo)
        put("cliAcompanhou", r.clienteAcompanhou)
        put("cliRecusou", r.clienteRecusouDados)
        put("acompNome", r.acompanhanteNome)
        put("acompDoc", r.acompanhanteDocumento)
        put("acompTel", r.acompanhanteTelefone)
        put("nominal", vazaoToJson(r.nominal))
        put("transicao", vazaoToJson(r.transicao))
        put("minima", vazaoToJson(r.minima))
    }.toString()

    fun fromJson(json: String): RascunhoEnsaio {
        val o = JSONObject(json)
        return RascunhoEnsaio(
            numeroHidrometro = o.optString("numeroHidrometro"),
            cliente = o.optString("cliente"),
            nomeCompanhia = o.optString("nomeCompanhia"),
            matricula = o.optString("matricula"),
            endereco = o.optString("endereco"),
            cidade = o.optString("cidade"),
            bairro = o.optString("bairro"),
            dataEnsaio = o.optString("dataEnsaio"),
            tecnicoResponsavel = o.optString("tecnico"),
            idadeHidrometro = o.optString("idade"),
            pressaoMedia = o.optString("pressao"),
            observacoes = o.optString("observacoes"),
            norma = o.optString("norma"),
            modeloSelecionadoId = o.optLong("modeloId"),
            realizado = o.optBoolean("realizado", true),
            motivoNaoRealizado = o.optString("motivo"),
            leituraFinalReprovado = o.optString("lfReprovado"),
            numeroSerieNovo = o.optString("serieNovo"),
            leituraInicialNovo = o.optString("liNovo"),
            clienteAcompanhou = o.optBoolean("cliAcompanhou", false),
            clienteRecusouDados = o.optBoolean("cliRecusou", false),
            acompanhanteNome = o.optString("acompNome"),
            acompanhanteDocumento = o.optString("acompDoc"),
            acompanhanteTelefone = o.optString("acompTel"),
            nominal = vazaoFromJson(o.optJSONObject("nominal")),
            transicao = vazaoFromJson(o.optJSONObject("transicao")),
            minima = vazaoFromJson(o.optJSONObject("minima"))
        )
    }

    private fun vazaoToJson(v: RascunhoVazao) = JSONObject().apply {
        put("m1", medicaoToJson(v.m1))
        put("m2", medicaoToJson(v.m2))
        put("m3", medicaoToJson(v.m3))
    }

    private fun medicaoToJson(m: RascunhoMedicao) = JSONObject().apply {
        put("e", m.escoamento)
        put("li", m.leituraInicial)
        put("lf", m.leituraFinal)
        put("pi", m.padraoInicial)
        put("pf", m.padraoFinal)
    }

    private fun vazaoFromJson(j: JSONObject?) = RascunhoVazao(
        m1 = medicaoFromJson(j?.optJSONObject("m1")),
        m2 = medicaoFromJson(j?.optJSONObject("m2")),
        m3 = medicaoFromJson(j?.optJSONObject("m3"))
    )

    private fun medicaoFromJson(j: JSONObject?) = RascunhoMedicao(
        escoamento = j?.optString("e") ?: "",
        leituraInicial = j?.optString("li") ?: "",
        leituraFinal = j?.optString("lf") ?: "",
        padraoInicial = j?.optString("pi") ?: "",
        padraoFinal = j?.optString("pf") ?: ""
    )
}
