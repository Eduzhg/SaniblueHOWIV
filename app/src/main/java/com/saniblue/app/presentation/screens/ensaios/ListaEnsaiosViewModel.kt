package com.saniblue.app.presentation.screens.ensaios

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saniblue.app.domain.model.Ensaio
import com.saniblue.app.domain.usecase.GetEnsaiosUseCase
import com.saniblue.app.domain.repository.EnsaioRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ListaEnsaiosUiState(
    val ensaios: List<Ensaio> = emptyList(),
    val isLoading: Boolean = true,
    val query: String = ""
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class ListaEnsaiosViewModel @Inject constructor(
    private val getEnsaios: GetEnsaiosUseCase,
    private val repository: EnsaioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListaEnsaiosUiState())
    val uiState: StateFlow<ListaEnsaiosUiState> = _uiState.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(300)
                .flatMapLatest { q -> getEnsaios(q) }
                .collect { ensaios ->
                    _uiState.value = _uiState.value.copy(
                        ensaios = ensaios,
                        isLoading = false
                    )
                }
        }
    }

    fun onQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchQuery.value = query
    }

    fun deleteEnsaio(id: Long) {
        viewModelScope.launch {
            repository.delete(id)
        }
    }
}
