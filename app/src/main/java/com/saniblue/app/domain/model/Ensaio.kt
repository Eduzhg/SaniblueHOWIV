package com.saniblue.app.domain.model

data class Ensaio(
    val id: Long = 0,
    val hidrometroModeloId: Long,
    val numeroHidrometro: String,
    val cliente: String,
    val matricula: String = "",
    val endereco: String = "",
    val cidade: String = "",
    val bairro: String = "",
    val dataEnsaio: String,
    val tecnicoResponsavel: String,
    val idadeHidrometro: String = "",
    val observacoes: String = "",
    // Nome da companhia de saneamento (ex.: "Samae - Blumenau (SC)")
    val nomeCompanhia: String = "",
    // Norma e método usados no ensaio
    val norma: NormaEnsaio = NormaEnsaio.PORTARIA_246,
    val metodoEnsaio: MetodoEnsaio = MetodoEnsaio.ESCOAMENTO_DIRETO,
    // Maleta usada e seu erro padrão (do certificado) aplicado no cálculo
    val maletaNome: String = "",
    val erroPadrao: Double = 0.0,
    // Pressão média durante o ensaio (mca)
    val pressaoMedia: String = "",
    // Ensaio não realizado + motivo (morador ausente, sem acesso, etc.)
    val realizado: Boolean = true,
    val motivoNaoRealizado: String = "",
    val resultadoFinal: ResultadoFinal = ResultadoFinal.PENDENTE,
    // Dados preenchidos apenas quando o hidrômetro é REPROVADO e substituído
    val leituraFinalReprovado: String = "",
    val numeroSerieNovo: String = "",
    val leituraInicialNovo: String = "",
    // Acompanhamento do ensaio pelo cliente (testemunha)
    val clienteAcompanhou: Boolean = false,
    val clienteRecusouDados: Boolean = false,
    val acompanhanteNome: String = "",
    val acompanhanteDocumento: String = "",
    val acompanhanteTelefone: String = "",
    val vazoes: List<VazaoEnsaio> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ResultadoFinal {
    APROVADO, REPROVADO, PENDENTE, NAO_REALIZADO
}

/** Motivos pré-definidos para um ensaio não realizado. */
object MotivosNaoRealizado {
    const val OUTRO = "Outro"
    val lista: List<String> = listOf(
        "Morador ausente",
        "Hidrômetro não localizado",
        "Animal no local (cachorro)",
        "Hidrômetro com impedimento de acesso",
        OUTRO
    )
}
