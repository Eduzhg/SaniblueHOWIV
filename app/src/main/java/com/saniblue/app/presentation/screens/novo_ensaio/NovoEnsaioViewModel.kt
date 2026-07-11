package com.saniblue.app.presentation.screens.novo_ensaio

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saniblue.app.domain.model.ClasseHidrometro
import com.saniblue.app.domain.model.Ensaio
import com.saniblue.app.domain.model.HidrometroModelo
import com.saniblue.app.domain.model.MetodoEnsaio
import com.saniblue.app.domain.model.NormaEnsaio
import com.saniblue.app.domain.model.ResultadoFinal
import com.saniblue.app.domain.model.TipoVazao
import com.saniblue.app.domain.model.VazaoEnsaio
import com.saniblue.app.domain.repository.EnsaioRepository
import com.saniblue.app.domain.repository.HidrometroRepository
import com.saniblue.app.domain.session.SessaoTecnico
import com.saniblue.app.domain.usecase.CalcularErroUseCase
import com.saniblue.app.domain.usecase.CalcularIdadeHidrometroUseCase
import com.saniblue.app.domain.usecase.SaveEnsaioUseCase
import com.saniblue.app.util.filtrarDecimal
import com.saniblue.app.util.filtrarSerialHidrometro
import com.saniblue.app.util.horaAtualDigits
import com.saniblue.app.util.isLetraCapacidadeConhecida
import com.saniblue.app.util.isSerialHidrometroValido
import com.saniblue.app.util.normaDoSerial
import com.saniblue.app.util.toDoubleLocale
import com.saniblue.app.util.FotoEnsaioHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
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
    val leituraInicialConfirmada: Boolean = false,
    // técnico já confirmou que a leitura final < inicial (hidrômetro em teste) está correta
    val leituraFinalMenorConfirmada: Boolean = false,
    // técnico já confirmou que o padrão final < inicial (padrão ultrassônico) está correto
    val padraoFinalMenorConfirmada: Boolean = false,
    // técnico já confirmou que o padrão inicial (menor que o final anterior) está correto
    val padraoInicialConfirmada: Boolean = false
)

enum class TipoAlertaLeitura { ERRO_ALTO, LEITURA_RETROCEDIDA, FINAL_MENOR_INICIAL }

/** Pedido de confirmação de leitura suspeita (provável erro de digitação ou hidrômetro "retrocedendo"). */
data class AlertaLeitura(
    val tipo: TipoVazao,
    val indice: Int,
    val tipoAlerta: TipoAlertaLeitura,
    val erroPct: Double = 0.0,
    val leituraAnterior: Double = 0.0,
    // FINAL_MENOR_INICIAL: identifica o par (padrão x hidrômetro) e os valores digitados
    val ehPadrao: Boolean = false,
    val valorInicial: Double = 0.0,
    val valorFinal: Double = 0.0
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
    val aprovado: Boolean? = null,
    // Vazão de referência não atingida em campo — técnico registra a vazão real usada
    val vazaoNaoAtingida: Boolean = false,
    val vazaoUtilizada: String = ""
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
    val observacoes: String = "",
    // Horário do ensaio (dígitos "HHmm") — opcional. Inicial vem do relógio do
    // aparelho ao abrir o ensaio; final, ao salvar. Ambas continuam editáveis.
    val horaInicial: String = "",
    val horaFinal: String = "",

    // Norma (selecionável no ensaio) e método/maleta (vêm do login — só leitura)
    val norma: NormaEnsaio = NormaEnsaio.PORTARIA_246,
    val metodoEnsaio: MetodoEnsaio = MetodoEnsaio.ESCOAMENTO_DIRETO,
    val maletaNome: String = "",
    val erroPadraoNominal: Double = 0.0,
    val erroPadraoTransicao: Double = 0.0,
    val erroPadraoMinima: Double = 0.0,

    // Pressão média (kg/cm²)
    val pressaoMedia: String = "",

    // Ensaio não realizado
    val realizado: Boolean = true,
    val motivoNaoRealizado: String = "",
    // Foto do local (não realizado): URI temporária em memória + path permanente após salvar
    val fotoTempUri: String = "",
    val fotoPath: String = "",

    // Alerta de leitura suspeita
    val alertaLeitura: AlertaLeitura? = null,

    // Modelo do hidrômetro — resolvido automaticamente pelo nº de série (norma + letra
    // + classe R quando aplicável), nunca escolhido manualmente
    val modeloSelecionadoId: Long = 0L,
    val modeloSelecionado: HidrometroModelo? = null,
    // Classe metrológica (R80/R100/R125) — só a Portaria 155 exige; identificada
    // visualmente pelo técnico no corpo do hidrômetro (não vem no nº de série)
    val classeR: ClasseHidrometro? = null,
    // Letra do nº de série não corresponde a nenhuma capacidade cadastrada (aviso, não bloqueia a digitação)
    val capacidadeNaoCadastrada: Boolean = false,

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

    // Acompanhamento do ensaio pelo cliente (testemunha)
    val clienteAcompanhou: Boolean = false,
    val clienteRecusouDados: Boolean = false,
    val acompanhanteNome: String = "",
    val acompanhanteDocumento: String = "",
    val acompanhanteTelefone: String = "",

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
) {
    /** Erro padrão (%) da maleta para a vazão informada. */
    fun erroPadraoPara(tipo: TipoVazao): Double = when (tipo) {
        TipoVazao.NOMINAL -> erroPadraoNominal
        TipoVazao.TRANSICAO -> erroPadraoTransicao
        TipoVazao.MINIMA -> erroPadraoMinima
    }
}

// Total de etapas do wizard: Cadastro, Nominal, Transição, Mínima, Resultado
const val TOTAL_PASSOS = 5

@OptIn(FlowPreview::class)
@HiltViewModel
class NovoEnsaioViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val saveEnsaio: SaveEnsaioUseCase,
    private val ensaioRepository: EnsaioRepository,
    private val hidrometroRepository: HidrometroRepository,
    private val calcularErro: CalcularErroUseCase,
    private val calcularIdade: CalcularIdadeHidrometroUseCase,
    private val sessao: SessaoTecnico
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        NovoEnsaioUiState(
            // Método e maleta vêm do login (sessão do turno)
            metodoEnsaio = sessao.metodoEnsaio,
            maletaNome = sessao.maleta.nome,
            erroPadraoNominal = sessao.maleta.erroPadraoNominal,
            erroPadraoTransicao = sessao.maleta.erroPadraoTransicao,
            erroPadraoMinima = sessao.maleta.erroPadraoMinima,
            // Data do ensaio já vem com a data atual (editável)
            dataEnsaio = dataAtualDigits(),
            // Hora inicial já vem do relógio do aparelho ao abrir o ensaio (editável)
            horaInicial = horaAtualDigits()
        )
    )
    val uiState: StateFlow<NovoEnsaioUiState> = _uiState.asStateFlow()

    /**
     * Id real do ensaio no banco, mantido em dia enquanto o técnico preenche o
     * formulário (rascunho contínuo — ver [autoSalvarRascunho]). 0L até a primeira
     * gravação de um ensaio novo; para edição/continuação, já vem preenchido.
     */
    private var rascunhoDbId: Long = 0L

    companion object {
        // Acima deste erro (%) a leitura é considerada suspeita (provável erro de
        // digitação) e pede confirmação. Ajuste conforme necessário.
        private const val LIMITE_ALERTA_ERRO = 50.0

        /** Data de hoje como dígitos "ddMMaaaa" (formato interno do campo Data). */
        private fun dataAtualDigits(): String =
            java.text.SimpleDateFormat("ddMMyyyy", Locale("pt", "BR")).format(java.util.Date())
    }

    init {
        // Grava o ensaio em andamento como um registro real na lista de ensaios
        // (não um rascunho separado) assim que houver um modelo resolvido — se o
        // app fechar/morrer no meio do teste, o técnico o encontra pendente na
        // Lista de Ensaios e continua de onde parou (Editar), sem risco de perder
        // 1h de medições atrás de um popup de "recuperar rascunho".
        viewModelScope.launch {
            uiState.debounce(500).collect { s ->
                if (!s.isSaved) autoSalvarRascunho(s)
            }
        }
    }

    /** Chamado pela tela ao abrir. id != 0 → edição ou continuação de um ensaio pendente. */
    fun iniciar(ensaioId: Long) {
        rascunhoDbId = ensaioId
        if (ensaioId != 0L) carregarEnsaio(ensaioId)
    }

    /**
     * Upsert silencioso do ensaio em andamento na tabela real de ensaios. Reaproveita
     * [rascunhoDbId] para atualizar sempre a mesma linha (nunca duplica). Só grava
     * quando há um modelo resolvido — o hidrômetro do ensaio é uma referência
     * obrigatória no banco (FK), então não há como gravar antes disso.
     */
    private fun autoSalvarRascunho(state: NovoEnsaioUiState) {
        if (state.modeloSelecionado == null) return
        if (!temAlgumDado(state)) return
        viewModelScope.launch {
            val ensaio = construirEnsaio(state, rascunhoDbId)
            runCatching { ensaioRepository.save(ensaio) }
                .onSuccess { novoId -> rascunhoDbId = novoId }
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
                observacoes = ensaio.observacoes,
                horaInicial = ensaio.horaInicial,
                horaFinal = ensaio.horaFinal,
                norma = ensaio.norma,
                metodoEnsaio = ensaio.metodoEnsaio,
                maletaNome = ensaio.maletaNome.ifBlank { sessao.maleta.nome },
                // Erros padrão: se o ensaio salvo não tinha maleta, usa os do build atual
                erroPadraoNominal = if (ensaio.maletaNome.isBlank()) sessao.maleta.erroPadraoNominal else ensaio.erroPadraoNominal,
                erroPadraoTransicao = if (ensaio.maletaNome.isBlank()) sessao.maleta.erroPadraoTransicao else ensaio.erroPadraoTransicao,
                erroPadraoMinima = if (ensaio.maletaNome.isBlank()) sessao.maleta.erroPadraoMinima else ensaio.erroPadraoMinima,
                pressaoMedia = ensaio.pressaoMedia,
                realizado = ensaio.realizado,
                motivoNaoRealizado = ensaio.motivoNaoRealizado,
                modeloSelecionadoId = ensaio.hidrometroModeloId,
                modeloSelecionado = modelo,
                classeR = modelo?.classeR,
                nominal = nominal?.toVazaoState() ?: VazaoState(),
                transicao = transicao?.toVazaoState() ?: VazaoState(),
                minima = minima?.toVazaoState() ?: VazaoState(),
                resultadoFinal = ensaio.resultadoFinal,
                leituraFinalReprovado = ensaio.leituraFinalReprovado,
                numeroSerieNovo = ensaio.numeroSerieNovo,
                leituraInicialNovo = ensaio.leituraInicialNovo,
                clienteAcompanhou = ensaio.clienteAcompanhou,
                clienteRecusouDados = ensaio.clienteRecusouDados,
                acompanhanteNome = ensaio.acompanhanteNome,
                acompanhanteDocumento = ensaio.acompanhanteDocumento,
                acompanhanteTelefone = ensaio.acompanhanteTelefone,
                fotoPath = ensaio.fotoPath
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
        aprovado = aprovado,
        vazaoNaoAtingida = vazaoNaoAtingida,
        vazaoUtilizada = vazaoUtilizada.toEditString()
    )

    private fun Double.toEditString() = if (this == 0.0) "" else this.toString()

    // --- Field updates ---

    fun updateNumeroHidrometro(v: String) {
        update {
            // Máscara adaptativa: 10 caracteres (Portaria 246) ou 12 (Portaria 155)
            val serial = v.filtrarSerialHidrometro()
            // Idade preenchida automaticamente a partir do nº de série (ex.: Y20B → fab. 2020)
            copy(numeroHidrometro = serial, idadeHidrometro = calcularIdade(serial) ?: idadeHidrometro)
        }
        resolverModeloAutomatico()
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
    fun updateObservacoes(v: String) = update { copy(observacoes = v) }
    fun updatePressaoMedia(v: String) = update { copy(pressaoMedia = v.filtrarDecimal()) }

    // Estado armazena só os 4 dígitos (ex: "0835"); a máscara HH:MM é visual
    fun updateHoraInicial(v: String) {
        val digits = v.filter { it.isDigit() }.take(4)
        update { copy(horaInicial = digits) }
    }
    fun updateHoraFinal(v: String) {
        val digits = v.filter { it.isDigit() }.take(4)
        update { copy(horaFinal = digits) }
    }

    // Ensaio não realizado
    fun setRealizado(realizado: Boolean) {
        update { copy(realizado = realizado) }
        atualizarResultadoFinal()
    }
    fun updateMotivoNaoRealizado(v: String) = update { copy(motivoNaoRealizado = v) }

    /** Chamado pela câmera após captura bem-sucedida para exibir preview. */
    fun setFotoTemp(uri: String) = update { copy(fotoTempUri = uri) }

    /** Descarta a foto temporária (usuário quer tirar outra). */
    fun descartarFotoTemp() = update { copy(fotoTempUri = "") }

    /**
     * Resolve o path definitivo da foto ao salvar:
     * - se já tem path permanente (ensaio editado), mantém
     * - se tem URI temp nova, salva na galeria agora
     */
    private fun resolverFotoPath(state: NovoEnsaioUiState): String {
        if (state.fotoPath.isNotBlank()) return state.fotoPath
        if (state.fotoTempUri.isBlank()) return ""
        val tempFile = FotoEnsaioHelper.obterArquivoTemp(appContext)
        if (!tempFile.exists() || tempFile.length() == 0L) return ""
        return FotoEnsaioHelper.salvarFoto(
            appContext,
            tempFile,
            state.numeroHidrometro,
            state.dataEnsaio.ifBlank { "sem_data" }
        )
    }

    // Dados de substituição (reprovado)
    fun updateLeituraFinalReprovado(v: String) = update { copy(leituraFinalReprovado = v.filtrarDecimal()) }
    // Mesmo formato do nº de série principal: Letra-NN-Letra-NNNNNN
    fun updateNumeroSerieNovo(v: String) = update { copy(numeroSerieNovo = v.filtrarSerialHidrometro()) }

    // Acompanhamento do ensaio pelo cliente
    fun setClienteAcompanhou(acompanhou: Boolean) = update {
        // Desligar limpa também a recusa e os dados digitados
        if (acompanhou) copy(clienteAcompanhou = true)
        else copy(
            clienteAcompanhou = false, clienteRecusouDados = false,
            acompanhanteNome = "", acompanhanteDocumento = "", acompanhanteTelefone = ""
        )
    }
    fun setClienteRecusouDados(recusou: Boolean) = update {
        // Recusa registra a negativa e limpa os dados
        if (recusou) copy(
            clienteRecusouDados = true,
            acompanhanteNome = "", acompanhanteDocumento = "", acompanhanteTelefone = ""
        ) else copy(clienteRecusouDados = false)
    }
    fun updateAcompanhanteNome(v: String) = update { copy(acompanhanteNome = v) }
    fun updateAcompanhanteDocumento(v: String) = update { copy(acompanhanteDocumento = v) }
    fun updateAcompanhanteTelefone(v: String) = update { copy(acompanhanteTelefone = v.filter { c -> c.isDigit() || c in " ()-+" }) }
    fun updateLeituraInicialNovo(v: String) = update { copy(leituraInicialNovo = v.filtrarDecimal()) }

    // Alerta de leitura suspeita
    fun confirmarAlerta() {
        val alerta = _uiState.value.alertaLeitura ?: return
        update {
            val vs = vazao(alerta.tipo)
            val atual = vs.medicao(alerta.indice)
            val m = when (alerta.tipoAlerta) {
                TipoAlertaLeitura.ERRO_ALTO -> atual.copy(altoErroConfirmado = true)
                TipoAlertaLeitura.LEITURA_RETROCEDIDA ->
                    if (alerta.ehPadrao) atual.copy(padraoInicialConfirmada = true)
                    else atual.copy(leituraInicialConfirmada = true)
                TipoAlertaLeitura.FINAL_MENOR_INICIAL ->
                    if (alerta.ehPadrao) atual.copy(padraoFinalMenorConfirmada = true)
                    else atual.copy(leituraFinalMenorConfirmada = true)
            }
            withVazao(alerta.tipo, vs.withMedicao(alerta.indice, m)).copy(alertaLeitura = null)
        }
    }
    fun descartarAlerta() = update { copy(alertaLeitura = null) }

    /**
     * Escolha manual da classe metrológica do hidrômetro (R80/R100/R125) — só existe
     * na Portaria 155; o técnico identifica visualmente no corpo do hidrômetro, pois
     * ela não é codificada no nº de série (ao contrário da norma e da capacidade).
     */
    fun selectClasseR(classe: ClasseHidrometro) {
        update { copy(classeR = classe) }
        resolverModeloAutomatico()
    }

    /**
     * Marca que a vazão de referência não foi atingida em campo (ex.: pressão
     * insuficiente) — não afeta o cálculo de aprovação, só documenta no laudo.
     * Desligar o aviso limpa a vazão utilizada digitada.
     */
    fun setVazaoNaoAtingida(tipo: TipoVazao, naoAtingida: Boolean) = update {
        val vs = vazao(tipo)
        withVazao(tipo, vs.copy(vazaoNaoAtingida = naoAtingida, vazaoUtilizada = if (naoAtingida) vs.vazaoUtilizada else ""))
    }

    fun updateVazaoUtilizada(tipo: TipoVazao, v: String) = update {
        withVazao(tipo, vazao(tipo).copy(vazaoUtilizada = v.filtrarDecimal(3)))
    }

    /**
     * Resolve automaticamente norma + modelo (capacidade/vazões de referência) a partir
     * do nº de série digitado. A Portaria 155, além da letra, também precisa da classe R
     * (escolhida manualmente) para determinar Q2/Q1. Se a letra não tiver catálogo
     * cadastrado, avisa (não bloqueia) e segue usando só os limites (%) da norma.
     */
    private fun resolverModeloAutomatico() {
        viewModelScope.launch {
            val s = _uiState.value
            val serialNorma = s.numeroHidrometro.normaDoSerial()

            if (serialNorma == null) {
                update { copy(modeloSelecionado = null, modeloSelecionadoId = 0L, capacidadeNaoCadastrada = false) }
                return@launch
            }

            // Serial de outra norma que a anteriormente detectada — reseta a classe R
            val normaMudou = serialNorma != s.norma
            val classeR = if (normaMudou) null else s.classeR
            if (normaMudou) update { copy(norma = serialNorma, classeR = null) }

            val letra = s.numeroHidrometro.first()
            if (!letra.isLetraCapacidadeConhecida(serialNorma)) {
                update { copy(modeloSelecionado = null, modeloSelecionadoId = 0L, capacidadeNaoCadastrada = true) }
                return@launch
            }

            // Portaria 155 com letra conhecida ainda precisa da classe R para Q2/Q1
            if (serialNorma == NormaEnsaio.PORTARIA_155 && classeR == null) {
                update { copy(modeloSelecionado = null, modeloSelecionadoId = 0L, capacidadeNaoCadastrada = false) }
                return@launch
            }

            val classeParaLookup = if (serialNorma == NormaEnsaio.PORTARIA_155) classeR else null
            val modelo = hidrometroRepository.getByNormaLetraClasse(serialNorma, letra, classeParaLookup)
            update {
                copy(
                    norma = serialNorma,
                    modeloSelecionado = modelo,
                    modeloSelecionadoId = modelo?.id ?: 0L,
                    capacidadeNaoCadastrada = modelo == null
                )
            }
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
                leituraInicialConfirmada = false,
                leituraFinalMenorConfirmada = false,
                padraoFinalMenorConfirmada = false,
                padraoInicialConfirmada = false
            )
            withVazao(tipo, vs.withMedicao(indice, novo))
        }
        recalcularVazao(tipo)
    }

    /**
     * Pré-preenche a leitura inicial (e o padrão inicial) da próxima medição desta mesma
     * vazão com a leitura final desta — chamado quando o técnico sai do campo "Leitura
     * Final"/"Padrão Final" (blur), nunca a cada dígito digitado. Só preenche se a próxima
     * ainda estiver em branco, nunca sobrescreve o que o técnico já digitou. A Medição 1
     * nunca recebe essa cópia: a vazão precisa ser regulada manualmente a cada novo teste.
     */
    fun preencherProximaLeituraInicial(tipo: TipoVazao, indice: Int) {
        if (indice >= 3) return
        update {
            val vs = vazao(tipo)
            val atual = vs.medicao(indice)
            val proxima = vs.medicao(indice + 1)
            val proximaAtualizada = proxima.copy(
                leituraInicial = if (proxima.leituraInicial.isBlank() && atual.leituraFinal.isNotBlank())
                    atual.leituraFinal else proxima.leituraInicial,
                padraoInicial = if (proxima.padraoInicial.isBlank() && atual.padraoFinal.isNotBlank())
                    atual.padraoFinal else proxima.padraoInicial
            )
            withVazao(tipo, vs.withMedicao(indice + 1, proximaAtualizada))
        }
    }

    private fun recalcularVazao(tipo: TipoVazao) {
        val state = _uiState.value
        val metodo = state.metodoEnsaio
        val norma = state.norma
        val vs = state.vazao(tipo)
        val erroPadrao = state.erroPadraoPara(tipo)

        fun processar(m: MedicaoState): Pair<MedicaoState, Double?> {
            val esc = escoamentoDe(m, metodo)
            val erro = calcularErroDaMedicao(m, metodo, erroPadrao)
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
     * Ao sair da leitura FINAL do hidrômetro em teste: se ela for menor que a inicial
     * da mesma medição, avisa (o hidrômetro não retrocede dentro de uma medição). Se não
     * for esse caso, segue para a verificação de erro alto.
     */
    fun verificarLeituraFinalSuspeita(tipo: TipoVazao, indice: Int) {
        if (_uiState.value.alertaLeitura != null) return
        val m = _uiState.value.vazao(tipo).medicao(indice)
        if (!m.leituraFinalMenorConfirmada) {
            val ini = m.leituraInicial.toDoubleLocale()
            val fin = m.leituraFinal.toDoubleLocale()
            if (ini != null && fin != null && fin < ini) {
                update {
                    copy(alertaLeitura = AlertaLeitura(
                        tipo, indice, TipoAlertaLeitura.FINAL_MENOR_INICIAL,
                        ehPadrao = false, valorInicial = ini, valorFinal = fin
                    ))
                }
                return
            }
        }
        verificarLeituraSuspeita(tipo, indice)
    }

    /**
     * Ao sair da leitura FINAL do padrão ultrassônico: se ela for menor que a inicial
     * da mesma medição, avisa (o padrão também não retrocede).
     */
    fun verificarPadraoFinalSuspeita(tipo: TipoVazao, indice: Int) {
        if (_uiState.value.alertaLeitura != null) return
        val m = _uiState.value.vazao(tipo).medicao(indice)
        if (m.padraoFinalMenorConfirmada) return
        val ini = m.padraoInicial.toDoubleLocale()
        val fin = m.padraoFinal.toDoubleLocale()
        if (ini != null && fin != null && fin < ini) {
            update {
                copy(alertaLeitura = AlertaLeitura(
                    tipo, indice, TipoAlertaLeitura.FINAL_MENOR_INICIAL,
                    ehPadrao = true, valorInicial = ini, valorFinal = fin
                ))
            }
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

    /**
     * Igual a [verificarLeituraInicialSuspeita], mas para o padrão ultrassônico da
     * maleta (método comparativo): a leitura inicial do padrão nesta medição não pode
     * ser menor que a leitura final do padrão na medição anterior da sequência.
     */
    fun verificarPadraoInicialSuspeita(tipo: TipoVazao, indice: Int) {
        if (_uiState.value.alertaLeitura != null) return
        val s = _uiState.value
        val m = s.vazao(tipo).medicao(indice)
        if (m.padraoInicialConfirmada) return
        val (tipoAnt, indiceAnt) = medicaoAnterior(tipo, indice) ?: return
        val anterior = s.vazao(tipoAnt).medicao(indiceAnt)
        val atual = m.padraoInicial.toDoubleLocale() ?: return
        val final = anterior.padraoFinal.toDoubleLocale() ?: return
        if (atual < final) {
            update {
                copy(alertaLeitura = AlertaLeitura(
                    tipo, indice, TipoAlertaLeitura.LEITURA_RETROCEDIDA,
                    leituraAnterior = final, ehPadrao = true
                ))
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

    private fun calcularErroDaMedicao(m: MedicaoState, metodo: MetodoEnsaio, erroPadrao: Double): Double? {
        val esc = escoamentoDe(m, metodo) ?: return null
        val ini = m.leituraInicial.toDoubleLocale() ?: return null
        val fin = m.leituraFinal.toDoubleLocale() ?: return null
        // Escoamento corrigido pelo erro padrão da maleta (específico da vazão)
        return calcularErro.calcularErro(esc, ini, fin, erroPadrao)
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
                if (s.norma == NormaEnsaio.PORTARIA_155 &&
                    s.numeroHidrometro.isSerialHidrometroValido() && s.classeR == null
                ) add("Classe do hidrômetro (R80/R100/R125)")
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
        "nomeCompanhia" -> "Companhia"
        "matricula" -> "Matrícula"
        "endereco" -> "Endereço"
        "bairro" -> "Bairro"
        "cidade" -> "Cidade"
        "idadeHidrometro" -> "Idade do Hidrômetro"
        "pressaoMedia" -> "Pressão Média"
        "tecnicoResponsavel" -> "Técnico"
        "dataEnsaio" -> "Data"
        "motivoNaoRealizado" -> "Motivo"
        "numeroSerieNovo" -> "Nº Série do Novo Hidrômetro"
        "acompanhanteNome" -> "Nome do Cliente (acompanhamento)"
        "classeR" -> "Classe do Hidrômetro"
        else -> key
    }

    fun salvar(ensaioId: Long = 0L) {
        val state = _uiState.value
        val erros = validar(state)
        if (erros.isNotEmpty()) {
            val faltantes = erros.keys.joinToString(", ") { rotuloCampo(it) }
            // Vai para a etapa onde estão os campos com problema: nº de série do
            // substituto e dados do acompanhante ficam no Resultado; o resto, no Cadastro
            val camposDoResultado = setOf("numeroSerieNovo", "acompanhanteNome")
            val passoDestino = if (erros.keys.all { it in camposDoResultado }) TOTAL_PASSOS - 1 else 0
            update {
                copy(
                    validationErrors = erros,
                    passoAtual = passoDestino,
                    mensagemAviso = "Corrija os campos: $faltantes"
                )
            }
            return
        }

        // Sem modelo resolvido (capacidade não cadastrada para a letra do serial) —
        // não há como definir o hidrometroModeloId; avisa em vez de falhar em silêncio
        if (state.modeloSelecionado == null) {
            update {
                copy(
                    passoAtual = 0,
                    mensagemAviso = "Não é possível salvar: capacidade do hidrômetro não cadastrada " +
                        "(letra '${state.numeroHidrometro.firstOrNull() ?: '?'}'). Fale com o suporte."
                )
            }
            return
        }

        viewModelScope.launch {
            update { copy(isLoading = true, error = null) }

            // IO thread: copia a foto temporária para a galeria antes de montar o Ensaio
            val fotoPath = if (!state.realizado) {
                withContext(Dispatchers.IO) { resolverFotoPath(state) }
            } else ""

            // Hora final: preenche automaticamente com o horário atual do aparelho se o
            // técnico ainda não tiver editado o campo manualmente
            val estadoFinal = if (state.horaFinal.isBlank()) {
                val horaFinal = horaAtualDigits()
                update { copy(horaFinal = horaFinal) }
                state.copy(horaFinal = horaFinal)
            } else state

            // Reaproveita o mesmo id do rascunho contínuo (se houver) — nunca duplica a linha
            val idFinal = if (rascunhoDbId != 0L) rascunhoDbId else ensaioId
            val ensaio = construirEnsaio(estadoFinal, idFinal, fotoPath)

            saveEnsaio(ensaio).fold(
                onSuccess = { id ->
                    rascunhoDbId = id
                    update { copy(isLoading = false, isSaved = true, savedId = id) }
                },
                onFailure = { e ->
                    update { copy(isLoading = false, error = e.message ?: "Erro ao salvar") }
                }
            )
        }
    }

    /** Monta o [Ensaio] a partir do estado atual — usado tanto pelo salvar final quanto pelo autosave. */
    private fun construirEnsaio(state: NovoEnsaioUiState, id: Long, fotoPath: String = state.fotoPath): Ensaio = Ensaio(
        id = id,
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
        observacoes = state.observacoes,
        horaInicial = state.horaInicial,
        horaFinal = state.horaFinal,
        norma = state.norma,
        metodoEnsaio = state.metodoEnsaio,
        maletaNome = state.maletaNome,
        erroPadraoNominal = state.erroPadraoNominal,
        erroPadraoTransicao = state.erroPadraoTransicao,
        erroPadraoMinima = state.erroPadraoMinima,
        pressaoMedia = state.pressaoMedia,
        realizado = state.realizado,
        motivoNaoRealizado = if (state.realizado) "" else state.motivoNaoRealizado,
        fotoPath = fotoPath,
        leituraFinalReprovado = state.leituraFinalReprovado,
        numeroSerieNovo = state.numeroSerieNovo,
        leituraInicialNovo = state.leituraInicialNovo,
        clienteAcompanhou = state.clienteAcompanhou,
        clienteRecusouDados = state.clienteRecusouDados,
        acompanhanteNome = state.acompanhanteNome,
        acompanhanteDocumento = state.acompanhanteDocumento,
        acompanhanteTelefone = state.acompanhanteTelefone,
        vazoes = if (state.realizado) buildVazoes(state) else emptyList(),
        resultadoFinal = state.resultadoFinal
    )

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
            aprovado  = vs.aprovado ?: false,
            vazaoNaoAtingida = vs.vazaoNaoAtingida,
            vazaoUtilizada = vs.vazaoUtilizada.d()
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
                erros["numeroHidrometro"] = "Formato inválido (ex.: A99A999999 ou A99AA9999999)"
        }
        // Portaria 155 com letra conhecida ainda exige a classe R (define Q2/Q1)
        if (state.norma == NormaEnsaio.PORTARIA_155 &&
            state.numeroHidrometro.isSerialHidrometroValido() &&
            state.classeR == null
        ) {
            erros["classeR"] = "Selecione a classe (R80/R100/R125)"
        }
        // Todos os campos do cadastro são obrigatórios
        if (state.cliente.isBlank()) erros["cliente"] = "Obrigatório"
        if (state.nomeCompanhia.isBlank()) erros["nomeCompanhia"] = "Obrigatório"
        if (state.matricula.isBlank()) erros["matricula"] = "Obrigatório"
        if (state.endereco.isBlank()) erros["endereco"] = "Obrigatório"
        if (state.bairro.isBlank()) erros["bairro"] = "Obrigatório"
        if (state.cidade.isBlank()) erros["cidade"] = "Obrigatório"
        if (state.idadeHidrometro.isBlank()) erros["idadeHidrometro"] = "Obrigatório"
        if (state.tecnicoResponsavel.isBlank()) erros["tecnicoResponsavel"] = "Obrigatório"

        val dataErro = validarData(state.dataEnsaio)
        if (dataErro != null) erros["dataEnsaio"] = dataErro

        // Condições do ensaio só fazem sentido quando ele foi realizado
        if (state.realizado) {
            if (state.pressaoMedia.isBlank()) erros["pressaoMedia"] = "Obrigatório"
        }

        // Ensaio não realizado exige motivo
        if (!state.realizado && state.motivoNaoRealizado.isBlank()) {
            erros["motivoNaoRealizado"] = "Selecione o motivo"
        }

        // Nº de série do hidrômetro substituto (reprovado): se informado, deve estar completo
        if (state.numeroSerieNovo.isNotBlank() && !state.numeroSerieNovo.isSerialHidrometroValido()) {
            erros["numeroSerieNovo"] = "Formato inválido (ex.: A99A999999)"
        }

        // Cliente acompanhou e não recusou: nome do acompanhante é obrigatório
        if (state.clienteAcompanhou && !state.clienteRecusouDados && state.acompanhanteNome.isBlank()) {
            erros["acompanhanteNome"] = "Obrigatório (ou marque a recusa)"
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

    /** Há dados suficientes no formulário para valer a pena gravar o rascunho contínuo. */
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
