package com.saniblue.app.presentation.screens.configuracoes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saniblue.app.domain.model.ResumoFotosEnsaio
import com.saniblue.app.domain.repository.EnsaioRepository
import com.saniblue.app.util.FotoLeituraHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ConfiguracoesViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val ensaioRepository: EnsaioRepository
) : ViewModel() {

    /** Total de ensaios no banco — usado no diálogo de confirmação e no rótulo do botão. */
    val totalEnsaios: StateFlow<Int> = ensaioRepository.getDashboardStats()
        .map { it.totalEnsaios }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem: StateFlow<String?> = _mensagem.asStateFlow()

    /** Quantas fotos de leitura estão gravadas e quanto ocupam no aparelho. */
    private val _usoFotos = MutableStateFlow(FotoLeituraHelper.UsoDisco(0, 0L))
    val usoFotos: StateFlow<FotoLeituraHelper.UsoDisco> = _usoFotos.asStateFlow()

    /** Ensaios que ainda guardam fotos, para a limpeza individual. */
    private val _fotosPorEnsaio = MutableStateFlow<List<ResumoFotosEnsaio>>(emptyList())
    val fotosPorEnsaio: StateFlow<List<ResumoFotosEnsaio>> = _fotosPorEnsaio.asStateFlow()

    init {
        atualizarFotos()
    }

    /** Relê o disco e o banco — chamado ao abrir a tela e depois de cada limpeza. */
    fun atualizarFotos() {
        viewModelScope.launch {
            _usoFotos.value = withContext(Dispatchers.IO) { FotoLeituraHelper.usoEmDisco(appContext) }
            _fotosPorEnsaio.value = runCatching { ensaioRepository.getResumoFotosLeitura() }
                .getOrDefault(emptyList())
        }
    }

    /** Apaga TODOS os ensaios (as vazões caem em cascata). Uso de teste/manutenção. */
    fun limparEnsaios() {
        viewModelScope.launch {
            val total = totalEnsaios.value
            runCatching { ensaioRepository.deleteAll() }
                .onSuccess {
                    // Sem ensaios, as fotos de leitura não têm mais a quem pertencer
                    withContext(Dispatchers.IO) { FotoLeituraHelper.apagarTudoDoDisco(appContext) }
                    _mensagem.value = "Ensaios apagados ($total)."
                    atualizarFotos()
                }
                .onFailure { _mensagem.value = "Falha ao apagar: ${it.message}" }
        }
    }

    /** Apaga as fotos de leitura de todos os ensaios, mantendo ensaios e medições. */
    fun limparTodasAsFotos() {
        viewModelScope.launch {
            runCatching {
                ensaioRepository.limparFotosLeitura(null)
                // Varre a pasta inteira: leva junto fotos órfãs que o banco não conhece
                withContext(Dispatchers.IO) { FotoLeituraHelper.apagarTudoDoDisco(appContext) }
            }.onSuccess { apagadas ->
                _mensagem.value = "Fotos de leitura apagadas ($apagadas)."
                atualizarFotos()
            }.onFailure { _mensagem.value = "Falha ao apagar fotos: ${it.message}" }
        }
    }

    /** Apaga as fotos de um ensaio só — as dos outros continuam. */
    fun limparFotosDoEnsaio(resumo: ResumoFotosEnsaio) {
        viewModelScope.launch {
            runCatching {
                val orfaos = ensaioRepository.limparFotosLeitura(resumo.ensaioId)
                withContext(Dispatchers.IO) { FotoLeituraHelper.apagarVarias(orfaos) }
                orfaos.size
            }.onSuccess { apagadas ->
                _mensagem.value = "Fotos do hidrômetro ${resumo.numeroHidrometro} apagadas ($apagadas)."
                atualizarFotos()
            }.onFailure { _mensagem.value = "Falha ao apagar fotos: ${it.message}" }
        }
    }

    fun limparMensagem() {
        _mensagem.value = null
    }
}
