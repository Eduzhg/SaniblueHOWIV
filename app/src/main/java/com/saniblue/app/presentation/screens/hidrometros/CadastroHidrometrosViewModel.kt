package com.saniblue.app.presentation.screens.hidrometros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saniblue.app.domain.model.HidrometroModelo
import com.saniblue.app.domain.repository.HidrometroRepository
import com.saniblue.app.util.toDoubleLocale
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CadastroHidrometrosUiState(
    val modelos: List<HidrometroModelo> = emptyList(),
    val isLoading: Boolean = true,
    val isEditing: Boolean = false,
    val modeloEmEdicao: HidrometroModelo? = null,
    val nome: String = "",
    val descricao: String = "",
    val vazaoNominal: String = "",
    val vazaoTransicao: String = "",
    val vazaoMinima: String = "",
    val error: String? = null,
    val saved: Boolean = false
)

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

    fun iniciarNovoModelo() {
        _uiState.value = _uiState.value.copy(
            isEditing = true,
            modeloEmEdicao = null,
            nome = "", descricao = "",
            vazaoNominal = "", vazaoTransicao = "", vazaoMinima = ""
        )
    }

    fun editarModelo(modelo: HidrometroModelo) {
        _uiState.value = _uiState.value.copy(
            isEditing = true,
            modeloEmEdicao = modelo,
            nome = modelo.nome,
            descricao = modelo.descricao,
            vazaoNominal = modelo.vazaoNominal.toString(),
            vazaoTransicao = modelo.vazaoTransicao.toString(),
            vazaoMinima = modelo.vazaoMinima.toString()
        )
    }

    fun cancelar() {
        _uiState.value = _uiState.value.copy(isEditing = false, saved = false)
    }

    fun onNomeChange(v: String) = update { copy(nome = v) }
    fun onDescricaoChange(v: String) = update { copy(descricao = v) }
    fun onVazaoNominalChange(v: String) = update { copy(vazaoNominal = v) }
    fun onVazaoTransicaoChange(v: String) = update { copy(vazaoTransicao = v) }
    fun onVazaoMinimaChange(v: String) = update { copy(vazaoMinima = v) }

    fun salvar() {
        val s = _uiState.value
        if (s.nome.isBlank() || s.vazaoNominal.isBlank()) {
            update { copy(error = "Nome e Vazão Nominal são obrigatórios") }
            return
        }
        viewModelScope.launch {
            val modelo = HidrometroModelo(
                id = s.modeloEmEdicao?.id ?: 0L,
                nome = s.nome,
                descricao = s.descricao,
                vazaoNominal   = s.vazaoNominal.toDoubleLocale()   ?: 0.0,
                vazaoTransicao = s.vazaoTransicao.toDoubleLocale() ?: 0.0,
                vazaoMinima    = s.vazaoMinima.toDoubleLocale()    ?: 0.0
            )
            repository.save(modelo)
            update { copy(isEditing = false, saved = true, error = null) }
        }
    }

    fun deletar(modelo: HidrometroModelo) {
        viewModelScope.launch { repository.delete(modelo) }
    }

    private fun update(block: CadastroHidrometrosUiState.() -> CadastroHidrometrosUiState) {
        _uiState.value = _uiState.value.block()
    }
}
