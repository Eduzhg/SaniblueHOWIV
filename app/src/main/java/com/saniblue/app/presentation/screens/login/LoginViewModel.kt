package com.saniblue.app.presentation.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saniblue.app.domain.model.Maleta
import com.saniblue.app.domain.model.MaletasDisponiveis
import com.saniblue.app.domain.model.MetodoEnsaio
import com.saniblue.app.domain.session.SessaoTecnico
import com.saniblue.app.domain.usecase.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val login: String = "",
    val senha: String = "",
    val metodoEnsaio: MetodoEnsaio = MetodoEnsaio.ESCOAMENTO_DIRETO,
    val maletas: List<Maleta> = MaletasDisponiveis.lista,
    val maletaSelecionada: Maleta = MaletasDisponiveis.padrao,
    val isLoading: Boolean = false,
    val error: String? = null,
    val loginSucesso: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val sessaoTecnico: SessaoTecnico
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onLoginChange(value: String) {
        _uiState.value = _uiState.value.copy(login = value, error = null)
    }

    fun onSenhaChange(value: String) {
        _uiState.value = _uiState.value.copy(senha = value, error = null)
    }

    fun onMetodoChange(metodo: MetodoEnsaio) {
        _uiState.value = _uiState.value.copy(metodoEnsaio = metodo)
    }

    fun onMaletaChange(maleta: Maleta) {
        _uiState.value = _uiState.value.copy(maletaSelecionada = maleta)
    }

    fun fazerLogin() {
        val state = _uiState.value
        // ⚠️ MODO TESTE: autenticação desabilitada — "Entrar" acessa o app direto,
        // sem validar usuário/senha. Para reativar o login, restaure a chamada a
        // authUseCase.login() (ver histórico do git / abaixo).
        // Grava as escolhas do turno (método + maleta) na sessão, como no fluxo real.
        sessaoTecnico.definir(state.metodoEnsaio, state.maletaSelecionada)
        _uiState.value = state.copy(isLoading = false, error = null, loginSucesso = true)
    }

    /*
    // Login real (desativado durante os testes):
    fun fazerLogin() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, error = null)
            val result = authUseCase.login(state.login.trim(), state.senha)
            result.fold(
                onSuccess = {
                    sessaoTecnico.definir(state.metodoEnsaio, state.maletaSelecionada)
                    _uiState.value = _uiState.value.copy(isLoading = false, loginSucesso = true)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Erro ao fazer login"
                    )
                }
            )
        }
    }
    */
}
