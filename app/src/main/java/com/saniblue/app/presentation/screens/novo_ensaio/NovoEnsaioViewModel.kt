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
import com.saniblue.app.data.local.datastore.RascunhoEnsaio
import com.saniblue.app.data.local.datastore.RascunhoEnsaioStore
import com.saniblue.app.data.local.datastore.RascunhoMedicao
import com.saniblue.app.data.local.datastore.RascunhoVazao
import com.saniblue.app.domain.repository.EnsaioRepository
import com.saniblue.app.domain.repository.HidrometroRepository
import com.saniblue.app.domain.session.SessaoTecnico
import com.saniblue.app.domain.usecase.CalcularErroUseCase
import com.saniblue.app.domain.usecase.CalcularIdadeHidrometroUseCase
import com.saniblue.app.domain.usecase.SaveEnsaioUseCase
import com.saniblue.app.util.filtrarDecimal
import com.saniblue.app.util.filtrarSerialHidrometro
import com.saniblue.app.util.isSerialHidrometroValido
import com.saniblue.app.util.toDoubleLocale
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
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
    val aprovado: Boolean? = null,
    // técnico já confirmou que a leitura com erro absurdo está correta
    val altoErroConfirmado: Boolean = false,
    // técnico já confirmou que a leitura inicial (menor que a final anterior) está correta
    val leituraInicialConfirmada: Boolean = false
)

enum class TipoAlertaLeitura { ERRO_ALTO, LEITURA_RETROCEDIDA }

/** Pedido de confirmação de leitura suspeita (provável erro de digitação ou hidrômetro "retrocedendo"). */
data class AlertaLeitura(
    val tipo: TipoVazao,
    val indice: Int,
    val tipoAlerta: TipoAlertaLeitura,
    val erroPct: Double = 0.0,
    val leituraAnterior: Double = 0.0
)

/**
 * Ordem sequencial de todas as medições do ensaio — o hidrômetro é o mesmo aparelho
 * lido continuamente, então a leitura inicial de uma medição deve ser >= a leitura
 * final da medição anterior nesta sequência (mesmo entre vazões diferentes).
 */
private val ORDEM_MEDICOES: List<Pair<TipoVazao, Int>> = listOf(
    TipoVazao.NOMINAL to 1, TipoVazao.NOMINAL to 2, TipoVazao.NOMINAL to 3,
    TipoVazao.TRANSICAO to 1, TipoVazao.TRANSICAO to 2, TipoVazao.TRANSICAO to 3,
    TipoVazao.MINIMA to 1, TipoVazao.MINIMA to 2, TipoVazao.MINIMA to 3
)

/** Medição anterior na sequência, ou null se for a primeira (Nominal M1 — nada a comparar). */
private fun medicaoAnterior(tipo: TipoVazao, indice: Int): Pair<TipoVazao, Int>? {
    val pos = ORDEM_MEDICOES.indexOf(tipo to indice)
    return if (pos <= 0) null else ORDEM_MEDICOES[pos - 1]
}

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

    // Norma (selecionável no ensaio) e método/maleta (vêm do login — só leitura)
    val norma: NormaEnsaio = NormaEnsaio.PORTARIA_246,
    val metodoEnsaio: MetodoEnsaio = MetodoEnsaio.ESCOAMENTO_DIRETO,
    val maletaNome: String = "",
    val erroPadrao: Double = 0.0,

    // Pressão média (mca)
    val pressaoMedia: String = "",

    // Ensaio não realizado
    val realizado: Boolean = true,
    val motivoNaoRealizado: String = "",

    // Alerta de leitura suspeita
    val alertaLeitura: AlertaLeitura? = null,

    // Rascunho restaurado de uma sessão anterior (mostra aviso ao técnico)
    val rascunhoRestaurado: Boolean = false,

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
    val validationErrors: Map<String, String> = emptyMap(),

    // Wizard (fluxo em etapas): Cadastro, Nominal, Transição, Mínima, Resultado
    val passoAtual: Int = 0,
    // Aviso transitório mostrado em snackbar (não bloqueia a navegação)
    val mensagemAviso: String? = null
)

// Total de etapas do wizard: Cadastro, Nominal, Transição, Mínima, Resultado
const val TOTAL_PASSOS = 5

@OptIn(FlowPreview::class)
@HiltViewModel
class NovoEnsaioViewModel @Inject constructor(
    private val saveEnsaio: SaveEnsaioUseCase,
    private val ensaioRepository: EnsaioRepository,
    private val hidrometroRepository: HidrometroRepository,
    private val calcularErro: CalcularErroUseCase,
    private val calcularIdade: CalcularIdadeHidrometroUseCase,
    private val sessao: SessaoTecnico,
    private val rascunhoStore: RascunhoEnsaioStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        NovoEnsaioUiState(
            // Método e maleta vêm do login (sessão do turno)
            metodoEnsaio = sessao.metodoEnsaio,
            maletaNome = sessao.maleta.nome,
            erroPadrao = sessao.maleta.erroPadrao
        )
    )
    val uiState: StateFlow<NovoEnsaioUiState> = _uiState.asStateFlow()

    // 0 = novo ensaio (autosave/restauração de rascunho ativos); >0 = edição
    private var ensaioIdAtual: Long = 0L

    companion object {
        // Acima deste erro (%) a leitura é considerada suspeita (provável erro de
        // digitação) e pede confirmação. Ajuste conforme necessário.
        private const val LIMITE_ALERTA_ERRO = 50.0
    }

    init {
        // Autosave do rascunho (com debounce) enquanto for um novo ensaio
        viewModelScope.launch {
            uiState.debounce(500).collect { s ->
                if (ensaioIdAtual == 0L && !s.isSaved && temAlgumDado(s)) {
                    runCatching { rascunhoStore.salvar(paraRascunho(s)) }
                }
            }
        }

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

    /** Chamado pela tela ao abrir. id != 0 → edição; id == 0 → novo (restaura rascunho). */
    fun iniciar(ensaioId: Long) {
        ensaioIdAtual = ensaioId
        if (ensaioId != 0L) {
            carregarEnsaio(ensaioId)
        } else {
            restaurarRascunho()
        }
    }

    private fun restaurarRascunho() {
        viewModelScope.launch {
            val rascunho = runCatching { rascunhoStore.ler() }.getOrNull() ?: return@launch
            aplicarRascunho(rascunho)
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
                maletaNome = ensaio.maletaNome.ifBlank { sessao.maleta.nome },
                erroPadrao = if (ensaio.maletaNome.isBlank()) sessao.maleta.erroPadrao else ensaio.erroPadrao,
                pressaoMedia = ensaio.pressaoMedia,
                realizado = ensaio.realizado,
                motivoNaoRealizado = ensaio.motivoNaoRealizado,
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
        // Máscara fixa Letra-NN-Letra-NNNNNN (10 caracteres)
        val serial = v.filtrarSerialHidrometro()
        // Idade preenchida automaticamente a partir do nº de série (ex.: Y20B → fab. 2020)
        copy(numeroHidrometro = serial, idadeHidrometro = calcularIdade(serial) ?: idadeHidrometro)
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
    fun updateTemperaturaAgua(v: String) = update { copy(temperaturaAgua = v.filtrarDecimal()) }
    fun updateObservacoes(v: String) = update { copy(observacoes = v) }
    fun updatePressaoMedia(v: String) = update { copy(pressaoMedia = v.filtrarDecimal()) }

    // Ensaio não realizado
    fun setRealizado(realizado: Boolean) {
        update { copy(realizado = realizado) }
        atualizarResultadoFinal()
    }
    fun updateMotivoNaoRealizado(v: String) = update { copy(motivoNaoRealizado = v) }

    // Dados de substituição (reprovado)
    fun updateLeituraFinalReprovado(v: String) = update { copy(leituraFinalReprovado = v.filtrarDecimal()) }
    // Mesmo formato do nº de série principal: Letra-NN-Letra-NNNNNN
    fun updateNumeroSerieNovo(v: String) = update { copy(numeroSerieNovo = v.filtrarSerialHidrometro()) }
    fun updateLeituraInicialNovo(v: String) = update { copy(leituraInicialNovo = v.filtrarDecimal()) }

    // Alerta de leitura suspeita
    fun confirmarAlerta() {
        val alerta = _uiState.value.alertaLeitura ?: return
        update {
            val vs = vazao(alerta.tipo)
            val atual = vs.medicao(alerta.indice)
            val m = when (alerta.tipoAlerta) {
                TipoAlertaLeitura.ERRO_ALTO -> atual.copy(altoErroConfirmado = true)
                TipoAlertaLeitura.LEITURA_RETROCEDIDA -> atual.copy(leituraInicialConfirmada = true)
            }
            withVazao(alerta.tipo, vs.withMedicao(alerta.indice, m)).copy(alertaLeitura = null)
        }
    }
    fun descartarAlerta() = update { copy(alertaLeitura = null) }

    fun selectNorma(norma: NormaEnsaio) {
        update { copy(norma = norma) }
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
                escoamento = escoamento?.filtrarDecimal(3) ?: m.escoamento,
                leituraInicial = inicial?.filtrarDecimal(3) ?: m.leituraInicial,
                leituraFinal = final?.filtrarDecimal(3) ?: m.leituraFinal,
                padraoInicial = padraoInicial?.filtrarDecimal(3) ?: m.padraoInicial,
                padraoFinal = padraoFinal?.filtrarDecimal(3) ?: m.padraoFinal,
                // qualquer alteração reabilita as checagens de leitura suspeita
                altoErroConfirmado = false,
                leituraInicialConfirmada = false
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

        // Vazão + resultado final em uma única emissão de estado (menos recomposições)
        update {
            val novo = withVazao(tipo, vs.copy(m1 = m1, m2 = m2, m3 = m3, erroMedio = medio, aprovado = aprovado))
            novo.copy(resultadoFinal = resultadoDe(novo))
        }
    }

    /**
     * Verifica uma medição específica e, se o erro estiver acima do limite e ainda
     * não confirmado, pede confirmação. Chamado quando o campo perde o foco (blur),
     * para não disparar enquanto o técnico ainda está digitando.
     */
    fun verificarLeituraSuspeita(tipo: TipoVazao, indice: Int) {
        if (_uiState.value.alertaLeitura != null) return
        val m = _uiState.value.vazao(tipo).medicao(indice)
        val erro = m.erro
        if (erro != null && !m.altoErroConfirmado && kotlin.math.abs(erro) > LIMITE_ALERTA_ERRO) {
            update { copy(alertaLeitura = AlertaLeitura(tipo, indice, TipoAlertaLeitura.ERRO_ALTO, erroPct = erro)) }
        }
    }

    /**
     * Verifica se a leitura inicial desta medição é menor que a leitura final da medição
     * anterior na sequência do ensaio (o hidrômetro é o mesmo aparelho, a leitura não
     * retrocede). Chamado quando o campo perde o foco. Não se aplica à primeira medição
     * (Nominal M1), que não tem leitura anterior para comparar.
     */
    fun verificarLeituraInicialSuspeita(tipo: TipoVazao, indice: Int) {
        if (_uiState.value.alertaLeitura != null) return
        val s = _uiState.value
        val m = s.vazao(tipo).medicao(indice)
        if (m.leituraInicialConfirmada) return
        val (tipoAnt, indiceAnt) = medicaoAnterior(tipo, indice) ?: return
        val anterior = s.vazao(tipoAnt).medicao(indiceAnt)
        val atual = m.leituraInicial.toDoubleLocale() ?: return
        val final = anterior.leituraFinal.toDoubleLocale() ?: return
        if (atual < final) {
            update {
                copy(alertaLeitura = AlertaLeitura(tipo, indice, TipoAlertaLeitura.LEITURA_RETROCEDIDA, leituraAnterior = final))
            }
        }
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
        // Escoamento corrigido pelo erro padrão da maleta
        return calcularErro.calcularErro(esc, ini, fin, _uiState.value.erroPadrao)
    }

    private fun atualizarResultadoFinal() = update { copy(resultadoFinal = resultadoDe(this)) }

    /** Resultado final derivado do estado (função pura — sem emitir estado). */
    private fun resultadoDe(s: NovoEnsaioUiState): ResultadoFinal {
        if (!s.realizado) return ResultadoFinal.NAO_REALIZADO
        val aprovacoes = listOf(s.nominal.aprovado, s.transicao.aprovado, s.minima.aprovado)
        return when {
            // Short-circuit: qualquer vazão completa e reprovada já reprova o ensaio
            aprovacoes.any { it == false } -> ResultadoFinal.REPROVADO
            aprovacoes.all { it == true } -> ResultadoFinal.APROVADO
            else -> ResultadoFinal.PENDENTE
        }
    }

    private fun recalcularTudo() {
        listOf(TipoVazao.NOMINAL, TipoVazao.TRANSICAO, TipoVazao.MINIMA).forEach {
            recalcularVazao(it)
        }
    }

    // --- Navegação do wizard ---
    // O autosave do rascunho (DataStore, com debounce no init) persiste o progresso
    // a cada mudança de estado — trocar de etapa já grava implicitamente.

    /** Vai direto para uma etapa (indicador de passos clicável). */
    fun irParaPasso(passo: Int) {
        val alvo = passo.coerceIn(0, TOTAL_PASSOS - 1)
        update { copy(passoAtual = alvo) }
    }

    /** Avança; avisa (sem bloquear) se a etapa atual tiver pendências. */
    fun proximoPasso() {
        val atual = _uiState.value.passoAtual
        avisarPendencias(atual)
        if (atual < TOTAL_PASSOS - 1) irParaPasso(atual + 1)
    }

    fun passoAnterior() {
        val atual = _uiState.value.passoAtual
        if (atual > 0) irParaPasso(atual - 1)
    }

    fun limparAviso() = update { copy(mensagemAviso = null) }

    /** Pendências de uma etapa — apenas informativo, nunca impede avançar. */
    private fun avisarPendencias(passo: Int) {
        val s = _uiState.value
        // Ensaio não realizado só exige cadastro/motivo — não há vazões a preencher
        if (!s.realizado && passo in 1..3) return
        val pend = when (passo) {
            0 -> buildList {
                if (s.numeroHidrometro.isBlank()) add("Nº Hidrômetro")
                if (s.cliente.isBlank()) add("Cliente")
                if (s.tecnicoResponsavel.isBlank()) add("Técnico")
                if (validarData(s.dataEnsaio) != null) add("Data")
                if (!s.realizado && s.motivoNaoRealizado.isBlank()) add("Motivo")
            }
            1 -> if (s.nominal.erroMedio == null) listOf("medições da ${s.norma.labelNominal}") else emptyList()
            2 -> if (s.transicao.erroMedio == null) listOf("medições da ${s.norma.labelTransicao}") else emptyList()
            3 -> if (s.minima.erroMedio == null) listOf("medições da ${s.norma.labelMinima}") else emptyList()
            else -> emptyList()
        }
        if (pend.isNotEmpty()) {
            update { copy(mensagemAviso = "Pendente: ${pend.joinToString(", ")}") }
        }
    }

    private fun rotuloCampo(key: String) = when (key) {
        "numeroHidrometro" -> "Nº Hidrômetro"
        "cliente" -> "Cliente"
        "tecnicoResponsavel" -> "Técnico"
        "dataEnsaio" -> "Data"
        "motivoNaoRealizado" -> "Motivo"
        "numeroSerieNovo" -> "Nº Série do Novo Hidrômetro"
        else -> key
    }

    fun salvar(ensaioId: Long = 0L) {
        val state = _uiState.value
        val erros = validar(state)
        if (erros.isNotEmpty()) {
            val faltantes = erros.keys.joinToString(", ") { rotuloCampo(it) }
            // Vai para a etapa onde estão os campos com problema: o nº de série do
            // substituto fica no Resultado; os demais obrigatórios, no Cadastro
            val passoDestino = if (erros.keys.all { it == "numeroSerieNovo" }) TOTAL_PASSOS - 1 else 0
            update {
                copy(
                    validationErrors = erros,
                    passoAtual = passoDestino,
                    mensagemAviso = "Corrija os campos: $faltantes"
                )
            }
            return
        }

        // Exige um modelo selecionado (define o hidrometroModeloId)
        if (state.modeloSelecionado == null) return

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
                maletaNome = state.maletaNome,
                erroPadrao = state.erroPadrao,
                pressaoMedia = state.pressaoMedia,
                realizado = state.realizado,
                motivoNaoRealizado = if (state.realizado) "" else state.motivoNaoRealizado,
                leituraFinalReprovado = state.leituraFinalReprovado,
                numeroSerieNovo = state.numeroSerieNovo,
                leituraInicialNovo = state.leituraInicialNovo,
                vazoes = if (state.realizado) buildVazoes(state) else emptyList(),
                resultadoFinal = state.resultadoFinal
            )

            saveEnsaio(ensaio).fold(
                onSuccess = { id ->
                    runCatching { rascunhoStore.limpar() }
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
        when {
            state.numeroHidrometro.isBlank() -> erros["numeroHidrometro"] = "Obrigatório"
            !state.numeroHidrometro.isSerialHidrometroValido() ->
                erros["numeroHidrometro"] = "Formato inválido (ex.: A99A999999)"
        }
        if (state.cliente.isBlank()) erros["cliente"] = "Obrigatório"
        if (state.tecnicoResponsavel.isBlank()) erros["tecnicoResponsavel"] = "Obrigatório"

        val dataErro = validarData(state.dataEnsaio)
        if (dataErro != null) erros["dataEnsaio"] = dataErro

        // Ensaio não realizado exige motivo
        if (!state.realizado && state.motivoNaoRealizado.isBlank()) {
            erros["motivoNaoRealizado"] = "Selecione o motivo"
        }

        // Nº de série do hidrômetro substituto (reprovado): se informado, deve estar completo
        if (state.numeroSerieNovo.isNotBlank() && !state.numeroSerieNovo.isSerialHidrometroValido()) {
            erros["numeroSerieNovo"] = "Formato inválido (ex.: A99A999999)"
        }

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

    // ─────────────────────────────────────────────────────────────────
    // Rascunho (autosave) — serialização dos campos editáveis em JSON
    // ─────────────────────────────────────────────────────────────────

    /** Descarta o rascunho atual e limpa o formulário. */
    fun descartarRascunho() {
        viewModelScope.launch { runCatching { rascunhoStore.limpar() } }
        _uiState.value = NovoEnsaioUiState(
            metodoEnsaio = sessao.metodoEnsaio,
            maletaNome = sessao.maleta.nome,
            erroPadrao = sessao.maleta.erroPadrao,
            modelos = _uiState.value.modelos,
            modeloSelecionadoId = _uiState.value.modelos.firstOrNull()?.id ?: 0L,
            modeloSelecionado = _uiState.value.modelos.firstOrNull()
        )
    }

    fun dispensarAvisoRascunho() = update { copy(rascunhoRestaurado = false) }

    private fun temAlgumDado(s: NovoEnsaioUiState): Boolean {
        if (s.numeroHidrometro.isNotBlank() || s.cliente.isNotBlank() ||
            s.matricula.isNotBlank() || s.nomeCompanhia.isNotBlank() ||
            s.motivoNaoRealizado.isNotBlank() || !s.realizado
        ) return true
        return listOf(s.nominal, s.transicao, s.minima).any { v ->
            listOf(v.m1, v.m2, v.m3).any {
                it.escoamento.isNotBlank() || it.leituraInicial.isNotBlank() ||
                    it.leituraFinal.isNotBlank() || it.padraoInicial.isNotBlank() ||
                    it.padraoFinal.isNotBlank()
            }
        }
    }

    private fun paraRascunho(s: NovoEnsaioUiState): RascunhoEnsaio {
        fun med(m: MedicaoState) = RascunhoMedicao(
            escoamento = m.escoamento,
            leituraInicial = m.leituraInicial,
            leituraFinal = m.leituraFinal,
            padraoInicial = m.padraoInicial,
            padraoFinal = m.padraoFinal
        )
        fun vaz(v: VazaoState) = RascunhoVazao(med(v.m1), med(v.m2), med(v.m3))
        return RascunhoEnsaio(
            numeroHidrometro = s.numeroHidrometro,
            cliente = s.cliente,
            nomeCompanhia = s.nomeCompanhia,
            matricula = s.matricula,
            endereco = s.endereco,
            cidade = s.cidade,
            bairro = s.bairro,
            dataEnsaio = s.dataEnsaio,
            tecnicoResponsavel = s.tecnicoResponsavel,
            idadeHidrometro = s.idadeHidrometro,
            temperaturaAgua = s.temperaturaAgua,
            pressaoMedia = s.pressaoMedia,
            observacoes = s.observacoes,
            norma = s.norma.name,
            modeloSelecionadoId = s.modeloSelecionadoId,
            realizado = s.realizado,
            motivoNaoRealizado = s.motivoNaoRealizado,
            leituraFinalReprovado = s.leituraFinalReprovado,
            numeroSerieNovo = s.numeroSerieNovo,
            leituraInicialNovo = s.leituraInicialNovo,
            nominal = vaz(s.nominal),
            transicao = vaz(s.transicao),
            minima = vaz(s.minima)
        )
    }

    private fun aplicarRascunho(r: RascunhoEnsaio) {
        fun med(m: RascunhoMedicao) = MedicaoState(
            escoamento = m.escoamento,
            leituraInicial = m.leituraInicial,
            leituraFinal = m.leituraFinal,
            padraoInicial = m.padraoInicial,
            padraoFinal = m.padraoFinal
        )
        fun vaz(v: RascunhoVazao) = VazaoState(med(v.m1), med(v.m2), med(v.m3))
        val modeloId = r.modeloSelecionadoId.takeIf { it != 0L } ?: _uiState.value.modeloSelecionadoId
        update {
            copy(
                numeroHidrometro = r.numeroHidrometro,
                cliente = r.cliente,
                nomeCompanhia = r.nomeCompanhia,
                matricula = r.matricula,
                endereco = r.endereco,
                cidade = r.cidade,
                bairro = r.bairro,
                dataEnsaio = r.dataEnsaio,
                tecnicoResponsavel = r.tecnicoResponsavel,
                idadeHidrometro = r.idadeHidrometro,
                temperaturaAgua = r.temperaturaAgua,
                pressaoMedia = r.pressaoMedia,
                observacoes = r.observacoes,
                norma = runCatching { NormaEnsaio.valueOf(r.norma) }.getOrDefault(norma),
                modeloSelecionadoId = modeloId,
                realizado = r.realizado,
                motivoNaoRealizado = r.motivoNaoRealizado,
                leituraFinalReprovado = r.leituraFinalReprovado,
                numeroSerieNovo = r.numeroSerieNovo,
                leituraInicialNovo = r.leituraInicialNovo,
                nominal = vaz(r.nominal),
                transicao = vaz(r.transicao),
                minima = vaz(r.minima),
                rascunhoRestaurado = true
            )
        }
        // Resolve o objeto do modelo a partir do id e recalcula tudo
        viewModelScope.launch {
            val modelo = hidrometroRepository.getById(modeloId)
            if (modelo != null) update { copy(modeloSelecionado = modelo) }
            recalcularTudo()
        }
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
