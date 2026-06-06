package com.saniblue.app.domain.repository

import com.saniblue.app.domain.model.Usuario

interface UsuarioRepository {
    suspend fun autenticar(login: String, senha: String): Usuario?
    suspend fun getById(id: Long): Usuario?
    suspend fun save(usuario: Usuario): Long
    suspend fun countUsuarios(): Int
}
