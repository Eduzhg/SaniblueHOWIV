package com.saniblue.app.domain.usecase

import com.saniblue.app.domain.model.Usuario
import com.saniblue.app.domain.repository.UsuarioRepository
import javax.inject.Inject

class AuthUseCase @Inject constructor(
    private val repository: UsuarioRepository
) {
    suspend fun login(login: String, senha: String): Result<Usuario> {
        if (login.isBlank() || senha.isBlank()) {
            return Result.failure(IllegalArgumentException("Login e senha são obrigatórios"))
        }
        val usuario = repository.autenticar(login, senha)
            ?: return Result.failure(Exception("Usuário ou senha incorretos"))
        return Result.success(usuario)
    }
}
