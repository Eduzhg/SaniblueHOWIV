package com.saniblue.app.presentation.screens.detalhes

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saniblue.app.domain.model.Ensaio
import com.saniblue.app.domain.model.HidrometroModelo
import com.saniblue.app.domain.repository.EnsaioRepository
import com.saniblue.app.domain.repository.HidrometroRepository
import com.saniblue.app.util.PdfGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class DetalhesUiState(
    val ensaio: Ensaio? = null,
    val modelo: HidrometroModelo? = null,
    val isLoading: Boolean = true,
    val pdfFile: File? = null,
    val isGeneratingPdf: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DetalhesEnsaioViewModel @Inject constructor(
    private val ensaioRepository: EnsaioRepository,
    private val hidrometroRepository: HidrometroRepository,
    private val pdfGenerator: PdfGenerator
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalhesUiState())
    val uiState: StateFlow<DetalhesUiState> = _uiState.asStateFlow()

    fun carregar(id: Long) {
        viewModelScope.launch {
            _uiState.value = DetalhesUiState(isLoading = true)
            val ensaio = ensaioRepository.getById(id)
            val modelo = ensaio?.let { hidrometroRepository.getById(it.hidrometroModeloId) }
            _uiState.value = DetalhesUiState(
                ensaio = ensaio,
                modelo = modelo,
                isLoading = false
            )
        }
    }

    fun gerarPdf(context: Context) {
        val ensaio = _uiState.value.ensaio ?: return
        val modelo = _uiState.value.modelo ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isGeneratingPdf = true)
            try {
                val file = pdfGenerator.gerarLaudo(context, ensaio, modelo)
                _uiState.value = _uiState.value.copy(pdfFile = file, isGeneratingPdf = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingPdf = false,
                    error = "Erro ao gerar PDF: ${e.message}"
                )
            }
        }
    }

    fun clearPdf() {
        _uiState.value = _uiState.value.copy(pdfFile = null)
    }
}
