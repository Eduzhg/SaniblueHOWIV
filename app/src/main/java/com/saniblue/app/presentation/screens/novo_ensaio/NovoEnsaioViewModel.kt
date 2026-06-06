package com.saniblue.app.presentation.screens.novo_ensaio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saniblue.app.domain.model.Ensaio
import com.saniblue.app.domain.model.HidrometroModelo
import com.saniblue.app.domain.model.MetodoEnsaio
import com.saniblue.app.domain.model.NormaEnsaio
import com.saniblue.app.domain.model.ResultadoFinal
import com.saniblue.app.domain.model.TipoVazao
import com.saniblue.app.domain.model.VazaoEnsaio
import com.saniblue.app.domain.repository.EnsaioRepository
import com.saniblue.app.domain.repository.HidrometroRepository
import com.saniblue.app.domain.usecase.CalcularErroUseCase
import com.saniblue.app.domain.usecase.SaveEnsaioUseCase
import com.saniblue.app.util.toDoubleLocale
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

data class MedicaoState(
    val escoamento: String = "",
    val leituraInicial: String = "",
    val leituraFinal: String = "",
    // Leituras do padrão ultrassônico (método COMPARATIVO_LEITURA)
    val padraoInicial: String = "",
    val padraoFinal: String = "",
    val erro: Double? = null,
    // aprovação individual da medição (baseada nos limites da norma selecionada)
    val aprovado: Boolean? = null
)

data class VazaoState(
    val m1: MedicaoState = MedicaoState(),
    val m2: MedicaoState = MedicaoState(),
    val m3: MedicaoState = MedicaoState(),
    val erroMedio: Double? = null,
    val aprovado: Boolean? = null
)

data class NovoEnsaioUiState(
    // Dados cadastrais
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
    val temperaturaAgua: String = "",
    val observacoes: String = "",

    // Norma e método do ensaio
    val norma: NormaEnsaio = NormaEnsaio.PORTARIA_246,
    val metodoEnsaio: MetodoEnsaio = MetodoEnsaio.ESCOAMENTO_DIRETO,

    // Modelo do hidrômetro (fornece as vazões de referência)
    val modelos: List<HidrometroModelo> = emptyList(),
    val modeloSelecionadoId: Long = 0L,
    val modeloSelecionado: HidrometroModelo? = null,

    // Medições por vazão
    val nominal: VazaoState = VazaoState(),
    val transicao: VazaoState = VazaoState(),
    val minima: VazaoState = VazaoState(),

    // Resultado
    val resultadoFinal: ResultadoFinal = ResultadoFinal.PENDENTE,

    // Dados de substituição (preenchidos quando REPROVADO)
    val leituraFinalReprovado: String = "",
    val numeroSerieNovo: String = "",
    val leituraInicialNovo: String = "",

    // Controle de UI
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val savedId: Long = 0L,
    val error: String? = null,
    val validationErrors: Map<String, String> = emptyMap()
)

@HiltViewModel
class NovoEnsaioViewModel @Inject constructor(
    private val saveEnsaio: SaveEnsaioUseCase,
    private val ensaioRepository: EnsaioRepository,
    private val hidrometroRepository: HidrometroRepository,
    private val calcularErro: CalcularErroUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NovoEnsaioUiState())
    val uiState: StateFlow<NovoEnsaioUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            hidrometroRepository.getAll().collect { modelos ->
                val primeiro = modelos.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    modelos = modelos,
                    modeloSelecionadoId = _uiState.value.modeloSelecionadoId.takeIf { it != 0L }
                        ?: primeiro?.id ?: 0L,
                    modeloSelecionado = _uiState.value.modeloSelecionado ?: primeiro
                )
            }
        }
    }

    fun carregarEnsaio(id: Long) {
        viewModelScope.launch {
            val ensaio = ensaioRepository.getById(id) ?: return@launch
            val modelo = hidrometroRepository.getById(ensaio.hidrometroModeloId)
            val nominal = ensaio.vazoes.find { it.tipoVazao == TipoVazao.NOMINAL }
            val transicao = ensaio.vazoes.find { it.tipoVazao == TipoVazao.TRANSICAO }
            val minima = ensaio.vazoes.find { it.tipoVazao == TipoVazao.MINIMA }

            _uiState.value = _uiState.value.copy(
                numeroHidrometro = ensaio.numeroHidrometro,
                cliente = ensaio.cliente,
                nomeCompanhia = ensaio.nomeCompanhia,
                matricula = ensaio.matricula,
                endereco = ensaio.endereco,
                cidade = ensaio.cidade,
                bairro = ensaio.bairro,
                dataEnsaio = ensaio.dataEnsaio.filter { it.isDigit() },
                tecnicoResponsavel = ensaio.tecnicoResponsavel,
                idadeHidrometro = ensaio.idadeHidrometro,
                temperaturaAgua = ensaio.temperaturaAgua,
                observacoes = ensaio.observacoes,
                norma = ensaio.norma,
                metodoEnsaio = ensaio.metodoEnsaio,
                modeloSelecionadoId = ensaio.hidrometroModeloId,
                modeloSelecionado = modelo,
                nominal = nominal?.toVazaoState() ?: VazaoState(),
                transicao = transicao?.toVazaoState() ?: VazaoState(),
                minima = minima?.toVazaoState() ?: VazaoState(),
                resultadoFinal = ensaio.resultadoFinal,
                leituraFinalReprovado = ensaio.leituraFinalReprovado,
                numeroSerieNovo = ensaio.numeroSerieNovo,
                leituraInicialNovo = ensaio.leituraInicialNovo
            )
            // Recalcular aprovações individuais após carregar os dados
            recalcularTudo()
        }
    }

    private fun VazaoEnsaio.toVazaoState() = VazaoState(
        m1 = MedicaoState(
            escoamento = m1Escoamento.toEditString(),
            leituraInicial = m1LeituraInicial.toEditString(),
            leituraFinal = m1LeituraFinal.toEditString(),
            padraoInicial = m1PadraoInicial.toEditString(),
            padraoFinal = m1PadraoFinal.toEditString(),
            erro = erro1
        ),
        m2 = MedicaoState(
            escoamento = m2Escoamento.toEditString(),
            leituraInicial = m2LeituraInicial.toEditString(),
            leituraFinal = m2LeituraFinal.toEditString(),
            padraoInicial = m2PadraoInicial.toEditString(),
            padraoFinal = m2PadraoFinal.toEditString(),
            erro = erro2
        ),
        m3 = MedicaoState(
            escoamento = m3Escoamento.toEditString(),
            leituraInicial = m3LeituraInicial.toEditString(),
            leituraFinal = m3LeituraFinal.toEditString(),
            padraoInicial = m3PadraoInicial.toEditString(),
            padraoFinal = m3PadraoFinal.toEditString(),
            erro = erro3
        ),
        erroMedio = erroMedio,
        aprovado = aprovado
    )

    private fun Double.toEditString() = if (this == 0.0) "" else this.toString()

    // --- Field updates ---

    fun updateNumeroHidrometro(v: String) = update {
        // Idade preenchida automaticamente a partir do nº de série (ex.: Y20B → fab. 2020)
        copy(numeroHidrometro = v, idadeHidrometro = calcularIdadePeloSerial(v) ?: idadeHidrometro)
    }
    fun updateCliente(v: String) = update { copy(cliente = v) }
    fun updateNomeCompanhia(v: String) = update { copy(nomeCompanhia = v) }
    fun updateMatricula(v: String) = update { copy(matricula = v) }
    fun updateEndereco(v: String) = update { copy(endereco = v) }
    fun updateCidade(v: String) = update { copy(cidade = v) }
    fun updateBairro(v: String) = update { copy(bairro = v) }
    // Estado armazena só os 8 dígitos (ex: "08052026"); a máscara DD/MM/AAAA é visual
    fun updateDataEnsaio(v: String) {
        val digits = v.filter { it.isDigit() }.take(8)
        update { copy(dataEnsaio = digits) }
    }
    fun updateTecnico(v: String) = update { copy(tecnicoResponsavel = v) }
    fun updateIdadeHidrometro(v: String) = update { copy(idadeHidrometro = v) }
    fun updateTemperaturaAgua(v: String) = update { copy(temperaturaAgua = v) }
    fun updateObservacoes(v: String) = update { copy(observacoes = v) }

    // Dados de substituição (reprovado)
    fun updateLeituraFinalReprovado(v: String) = update { copy(leituraFinalReprovado = v) }
    fun updateNumeroSerieNovo(v: String) = update { copy(numeroSerieNovo = v) }
    fun updateLeituraInicialNovo(v: String) = update { copy(leituraInicialNovo = v) }

    fun selectNorma(norma: NormaEnsaio) {
        update { copy(norma = norma) }
        recalcularTudo()
    }

    fun selectMetodo(metodo: MetodoEnsaio) {
        update { copy(metodoEnsaio = metodo) }
        recalcularTudo()
    }

    fun selectModelo(id: Long) {
        viewModelScope.launch {
            val modelo = hidrometroRepository.getById(id)
            update { copy(modeloSelecionadoId = id, modeloSelecionado = modelo) }
            recalcularTudo()
        }
    }

    // --- Medição updates (genérico) com recálculo em tempo real ---

    fun updateMedicao(
        tipo: TipoVazao,
        indice: Int,
        escoamento: String? = null,
        inicial: String? = null,
        final: String? = null,
        padraoInicial: String? = null,
        padraoFinal: String? = null
    ) {
        update {
            val vs = vazao(tipo)
            val m = vs.medicao(indice)
            val novo = m.copy(
                escoamento = escoamento ?: m.escoamento,
                leituraInicial = inicial ?: m.leituraInicial,
                leituraFinal = final ?: m.leituraFinal,
                padraoInicial = padraoInicial ?: m.padraoInicial,
                padraoFinal = padraoFinal ?: m.padraoFinal
            )
            withVazao(tipo, vs.withMedicao(indice, novo))
        }
        recalcularVazao(tipo)
    }

    private fun recalcularVazao(tipo: TipoVazao) {
        val state = _uiState.value
        val metodo = state.metodoEnsaio
        val norma = state.norma
        val vs = state.vazao(tipo)

        fun processar(m: MedicaoState): Pair<MedicaoState, Double?> {
            val esc = escoamentoDe(m, metodo)
            val erro = calcularErroDaMedicao(m, metodo)
            // No método comparativo o escoamento é calculado e exibido (read-only)
            val escStr = if (metodo == MetodoEnsaio.COMPARATIVO_LEITURA) {
                esc?.let { formatNum(it) } ?: ""
            } else m.escoamento
            val aprov = erro?.let { calcularErro.isVazaoAprovada(it, tipo, norma) }
            return m.copy(escoamento = escStr, erro = erro, aprovado = aprov) to erro
        }

        val (m1, e1) = processar(vs.m1)
        val (m2, e2) = processar(vs.m2)
        val (m3, e3) = processar(vs.m3)

        val medio = if (e1 != null && e2 != null && e3 != null) (e1 + e2 + e3) / 3.0 else null
        val aprovado = medio?.let { calcularErro.isVazaoAprovada(it, tipo, norma) }

        update {
            withVazao(tipo, vs.copy(m1 = m1, m2 = m2, m3 = m3, erroMedio = medio, aprovado = aprovado))
        }
        atualizarResultadoFinal()
    }

    /** Volume escoado de uma medição, conforme o método. */
    private fun escoamentoDe(m: MedicaoState, metodo: MetodoEnsaio): Double? {
        return if (metodo == MetodoEnsaio.COMPARATIVO_LEITURA) {
            val pi = m.padraoInicial.toDoubleLocale() ?: return null
            val pf = m.padraoFinal.toDoubleLocale() ?: return null
            (pf - pi).takeIf { it > 0.0 }
        } else {
            m.escoamento.toDoubleLocale()?.takeIf { it > 0.0 }
        }
    }

    private fun calcularErroDaMedicao(m: MedicaoState, metodo: MetodoEnsaio): Double? {
        val esc = escoamentoDe(m, metodo) ?: return null
        val ini = m.leituraInicial.toDoubleLocale() ?: return null
        val fin = m.leituraFinal.toDoubleLocale() ?: return null
        return calcularErro.calcularErro(esc, ini, fin)
    }

    private fun atualizarResultadoFinal() {
        val s = _uiState.value
        val aprovacoes = listOf(s.nominal.aprovado, s.transicao.aprovado, s.minima.aprovado)
        val resultado = when {
            aprovacoes.any { it == null } -> ResultadoFinal.PENDENTE
            aprovacoes.all { it == true } -> ResultadoFinal.APROVADO
            else -> ResultadoFinal.REPROVADO
        }
        update { copy(resultadoFinal = resultado) }
    }

    private fun recalcularTudo() {
        listOf(TipoVazao.NOMINAL, TipoVazao.TRANSICAO, TipoVazao.MINIMA).forEach {
            recalcularVazao(it)
        }
    }

    fun salvar(ensaioId: Long = 0L) {
        val state = _uiState.value
        val erros = validar(state)
        if (erros.isNotEmpty()) {
            update { copy(validationErrors = erros) }
            return
        }

        val modelo = state.modeloSelecionado ?: return

        viewModelScope.launch {
            update { copy(isLoading = true, error = null) }

            val ensaio = Ensaio(
                id = ensaioId,
                hidrometroModeloId = state.modeloSelecionadoId,
                numeroHidrometro = state.numeroHidrometro,
                cliente = state.cliente,
                nomeCompanhia = state.nomeCompanhia,
                matricula = state.matricula,
                endereco = state.endereco,
                cidade = state.cidade,
                bairro = state.bairro,
                dataEnsaio = formatarData(state.dataEnsaio),
                tecnicoResponsavel = state.tecnicoResponsavel,
                idadeHidrometro = state.idadeHidrometro,
                temperaturaAgua = state.temperaturaAgua,
                observacoes = state.observacoes,
                norma = state.norma,
                metodoEnsaio = state.metodoEnsaio,
                leituraFinalReprovado = state.leituraFinalReprovado,
                numeroSerieNovo = state.numeroSerieNovo,
                leituraInicialNovo = state.leituraInicialNovo,
                vazoes = buildVazoes(state),
                fotos = emptyList(),
                resultadoFinal = state.resultadoFinal
            )

            saveEnsaio(ensaio, modelo).fold(
                onSuccess = { id ->
                    update { copy(isLoading = false, isSaved = true, savedId = id) }
                },
                onFailure = { e ->
                    update { copy(isLoading = false, error = e.message ?: "Erro ao salvar") }
                }
            )
        }
    }

    private fun buildVazoes(state: NovoEnsaioUiState): List<VazaoEnsaio> {
        fun String.d() = toDoubleLocale() ?: 0.0
        fun toVazao(tipo: TipoVazao, vs: VazaoState) = VazaoEnsaio(
            tipoVazao = tipo,
            m1Escoamento     = vs.m1.escoamento.d(),
            m1LeituraInicial = vs.m1.leituraInicial.d(),
            m1LeituraFinal   = vs.m1.leituraFinal.d(),
            m2Escoamento     = vs.m2.escoamento.d(),
            m2LeituraInicial = vs.m2.leituraInicial.d(),
            m2LeituraFinal   = vs.m2.leituraFinal.d(),
            m3Escoamento     = vs.m3.escoamento.d(),
            m3LeituraInicial = vs.m3.leituraInicial.d(),
            m3LeituraFinal   = vs.m3.leituraFinal.d(),
            m1PadraoInicial  = vs.m1.padraoInicial.d(),
            m1PadraoFinal    = vs.m1.padraoFinal.d(),
            m2PadraoInicial  = vs.m2.padraoInicial.d(),
            m2PadraoFinal    = vs.m2.padraoFinal.d(),
            m3PadraoInicial  = vs.m3.padraoInicial.d(),
            m3PadraoFinal    = vs.m3.padraoFinal.d(),
            erro1    = vs.m1.erro ?: 0.0,
            erro2    = vs.m2.erro ?: 0.0,
            erro3    = vs.m3.erro ?: 0.0,
            erroMedio = vs.erroMedio ?: 0.0,
            aprovado  = vs.aprovado ?: false
        )
        return listOf(
            toVazao(TipoVazao.NOMINAL, state.nominal),
            toVazao(TipoVazao.TRANSICAO, state.transicao),
            toVazao(TipoVazao.MINIMA, state.minima)
        )
    }

    private fun validar(state: NovoEnsaioUiState): Map<String, String> {
        val erros = mutableMapOf<String, String>()
        if (state.numeroHidrometro.isBlank()) erros["numeroHidrometro"] = "Obrigatório"
        if (state.cliente.isBlank()) erros["cliente"] = "Obrigatório"
        if (state.tecnicoResponsavel.isBlank()) erros["tecnicoResponsavel"] = "Obrigatório"

        val dataErro = validarData(state.dataEnsaio)
        if (dataErro != null) erros["dataEnsaio"] = dataErro

        return erros
    }

    private fun validarData(digits: String): String? {
        if (digits.isBlank()) return "Obrigatório"
        if (digits.length < 8) return "Data incompleta — informe DD/MM/AAAA"
        val dia = digits.take(2).toIntOrNull() ?: return "Data inválida"
        val mes = digits.drop(2).take(2).toIntOrNull() ?: return "Data inválida"
        val ano = digits.drop(4).toIntOrNull() ?: return "Data inválida"
        if (mes !in 1..12) return "Mês inválido (01-12)"
        val maxDia = when (mes) {
            2    -> if (ano % 4 == 0 && (ano % 100 != 0 || ano % 400 == 0)) 29 else 28
            4, 6, 9, 11 -> 30
            else -> 31
        }
        if (dia !in 1..maxDia) return "Dia inválido para o mês"
        if (ano < 2000 || ano > 2100) return "Ano inválido (2000-2100)"
        return null
    }

    private fun formatarData(digits: String): String {
        if (digits.length < 8) return digits
        return "${digits.take(2)}/${digits.drop(2).take(2)}/${digits.drop(4)}"
    }

    /**
     * Calcula a idade do hidrômetro a partir do nº de série.
     * Os 2 primeiros dígitos representam o ano de fabricação (ex.: Y20B → 2020).
     * Idade = ano atual − ano de fabricação.
     */
    private fun calcularIdadePeloSerial(serial: String): String? {
        val yy = Regex("(\\d{2})").find(serial)?.groupValues?.get(1)?.toIntOrNull() ?: return null
        val anoAtual = Calendar.getInstance().get(Calendar.YEAR)
        var anoFab = 2000 + yy
        if (anoFab > anoAtual) anoFab = 1900 + yy
        val idade = anoAtual - anoFab
        if (idade < 0) return null
        return "$idade ano(s) — fab. $anoFab"
    }

    private fun formatNum(d: Double): String {
        val s = String.format(Locale.US, "%.3f", d)
        return s.trimEnd('0').trimEnd('.')
    }

    // --- Helpers de acesso às vazões/medições ---

    private fun NovoEnsaioUiState.vazao(tipo: TipoVazao): VazaoState = when (tipo) {
        TipoVazao.NOMINAL -> nominal
        TipoVazao.TRANSICAO -> transicao
        TipoVazao.MINIMA -> minima
    }

    private fun NovoEnsaioUiState.withVazao(tipo: TipoVazao, v: VazaoState): NovoEnsaioUiState =
        when (tipo) {
            TipoVazao.NOMINAL -> copy(nominal = v)
            TipoVazao.TRANSICAO -> copy(transicao = v)
            TipoVazao.MINIMA -> copy(minima = v)
        }

    private fun VazaoState.medicao(indice: Int): MedicaoState = when (indice) {
        1 -> m1; 2 -> m2; else -> m3
    }

    private fun VazaoState.withMedicao(indice: Int, m: MedicaoState): VazaoState = when (indice) {
        1 -> copy(m1 = m); 2 -> copy(m2 = m); else -> copy(m3 = m)
    }

    private fun update(block: NovoEnsaioUiState.() -> NovoEnsaioUiState) {
        _uiState.value = _uiState.value.block()
    }
}
