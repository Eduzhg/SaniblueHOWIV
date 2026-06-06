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
    val temperaturaAgua: String = "",
    val observacoes: String = "",
    // Nome da companhia de saneamento (ex.: "Samae - Blumenau (SC)")
    val nomeCompanhia: String = "",
    // Norma e método usados no ensaio
    val norma: NormaEnsaio = NormaEnsaio.PORTARIA_246,
    val metodoEnsaio: MetodoEnsaio = MetodoEnsaio.ESCOAMENTO_DIRETO,
    val resultadoFinal: ResultadoFinal = ResultadoFinal.PENDENTE,
    // Dados preenchidos apenas quando o hidrômetro é REPROVADO e substituído
    val leituraFinalReprovado: String = "",
    val numeroSerieNovo: String = "",
    val leituraInicialNovo: String = "",
    val vazoes: List<VazaoEnsaio> = emptyList(),
    val fotos: List<FotoEnsaio> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ResultadoFinal {
    APROVADO, REPROVADO, PENDENTE
}
