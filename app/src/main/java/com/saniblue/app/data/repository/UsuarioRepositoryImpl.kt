package com.saniblue.app.data.repository

import com.saniblue.app.data.local.dao.UsuarioDao
import com.saniblue.app.data.local.entity.UsuarioEntity
import com.saniblue.app.domain.model.Usuario
import com.saniblue.app.domain.repository.UsuarioRepository
import com.saniblue.app.util.HashUtils
import javax.inject.Inject

class UsuarioRepositoryImpl @Inject constructor(
    private val dao: UsuarioDao
) : UsuarioRepository {

    override suspend fun autenticar(login: String, senha: String): Usuario? {
        val senhaHash = HashUtils.sha256(senha)
        return dao.autenticar(login, senhaHash)?.toDomain()
    }

    override suspend fun getById(id: Long): Usuario? =
        dao.getById(id)?.toDomain()

    override suspend fun save(usuario: Usuario): Long {
        val entity = usuario.toEntity()
        return if (usuario.id == 0L) {
            dao.insert(entity)
        } else {
            dao.update(entity)
            usuario.id
        }
    }

    override suspend fun countUsuarios(): Int {
        var count = 0
        dao.getAllAtivos().collect { list -> count = list.size }
        return count
    }

    private fun UsuarioEntity.toDomain() = Usuario(
        id = id,
        nome = nome,
        login = login,
        cargo = cargo,
        email = email,
        ativo = ativo
    )

    private fun Usuario.toEntity() = UsuarioEntity(
        id = id,
        nome = nome,
        login = login,
        senhaHash = senhaHash ?: "",
        cargo = cargo,
        email = email,
        ativo = ativo
    )
}
