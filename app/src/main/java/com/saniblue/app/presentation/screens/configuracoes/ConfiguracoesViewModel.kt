package com.saniblue.app.presentation.screens.configuracoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saniblue.app.domain.repository.EnsaioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfiguracoesViewModel @Inject constructor(
    private val ensaioRepository: EnsaioRepository
) : ViewModel() {

    /** Total de ensaios no banco — usado no diálogo de confirmação e no rótulo do botão. */
    val totalEnsaios: StateFlow<Int> = ensaioRepository.getDashboardStats()
        .map { it.totalEnsaios }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    private val _mensagem = MutableStateFlow<String?>(null)
    val mensagem: StateFlow<String?> = _mensagem.asStateFlow()

    /** Apaga TODOS os ensaios (as vazões caem em cascata). Uso de teste/manutenção. */
    fun limparEnsaios() {
        viewModelScope.launch {
            val total = totalEnsaios.value
            runCatching { ensaioRepository.deleteAll() }
                .onSuccess { _mensagem.value = "Ensaios apagados ($total)." }
                .onFailure { _mensagem.value = "Falha ao apagar: ${it.message}" }
        }
    }

    fun limparMensagem() {
        _mensagem.value = null
    }
}
