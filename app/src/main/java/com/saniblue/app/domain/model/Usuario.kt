package com.saniblue.app.domain.model

data class Usuario(
    val id: Long = 0,
    val nome: String,
    val login: String,
    val cargo: String = "Técnico",
    val email: String = "",
    val ativo: Boolean = true,
    // Apenas para criação — nunca exposto em flows
    val senhaHash: String? = null
)
