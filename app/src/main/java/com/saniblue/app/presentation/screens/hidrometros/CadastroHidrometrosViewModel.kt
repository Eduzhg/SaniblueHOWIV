package com.saniblue.app.presentation.screens.hidrometros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saniblue.app.domain.model.HidrometroModelo
import com.saniblue.app.domain.repository.HidrometroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CadastroHidrometrosUiState(
    val modelos: List<HidrometroModelo> = emptyList(),
    val isLoading: Boolean = true
)

/**
 * O catálogo de capacidades (norma + letra + classe R) é fixo e resolvido
 * automaticamente pelo nº de série no Novo Ensaio — esta tela é só consulta.
 */
@HiltViewModel
class CadastroHidrometrosViewModel @Inject constructor(
    private val repository: HidrometroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CadastroHidrometrosUiState())
    val uiState: StateFlow<CadastroHidrometrosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAll().collect { modelos ->
                _uiState.value = _uiState.value.copy(modelos = modelos, isLoading = false)
            }
        }
    }
}
