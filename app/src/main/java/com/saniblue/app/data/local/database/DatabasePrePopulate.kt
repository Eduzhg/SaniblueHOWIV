package com.saniblue.app.data.local.database

import com.saniblue.app.data.local.entity.HidrometroModeloEntity
import com.saniblue.app.data.local.entity.UsuarioEntity
import com.saniblue.app.util.HashUtils

/**
 * Dados iniciais inseridos no primeiro uso do app.
 * Adicionar novos modelos de hidrômetro aqui sem alterar lógica de negócio.
 */
object DatabasePrePopulate {

    fun getModelosHidrometro(): List<HidrometroModeloEntity> = listOf(
        HidrometroModeloEntity(
            nome = "Hidrômetro 3 m³/h",
            descricao = "Hidrômetro classe B - Vazão nominal 3 m³/h",
            vazaoNominal = 1500.0,    // L/h
            vazaoTransicao = 120.0,   // L/h
            vazaoMinima = 30.0,       // L/h
            limiteNominalMin = -5.0,
            limiteNominalMax = 5.0,
            limiteTransicaoMin = -5.0,
            limiteTransicaoMax = 5.0,
            limiteMinimaMin = -10.0,
            limiteMinimaMax = 10.0
        ),
        HidrometroModeloEntity(
            nome = "Hidrômetro 5 m³/h",
            descricao = "Hidrômetro classe B - Vazão nominal 5 m³/h",
            vazaoNominal = 2500.0,
            vazaoTransicao = 200.0,
            vazaoMinima = 50.0,
            limiteNominalMin = -5.0,
            limiteNominalMax = 5.0,
            limiteTransicaoMin = -5.0,
            limiteTransicaoMax = 5.0,
            limiteMinimaMin = -10.0,
            limiteMinimaMax = 10.0
        ),
        HidrometroModeloEntity(
            nome = "Hidrômetro 10 m³/h",
            descricao = "Hidrômetro classe C - Vazão nominal 10 m³/h",
            vazaoNominal = 5000.0,
            vazaoTransicao = 400.0,
            vazaoMinima = 100.0,
            limiteNominalMin = -5.0,
            limiteNominalMax = 5.0,
            limiteTransicaoMin = -5.0,
            limiteTransicaoMax = 5.0,
            limiteMinimaMin = -10.0,
            limiteMinimaMax = 10.0
        )
    )

    fun getUsuariosDefault(): List<UsuarioEntity> = listOf(
        UsuarioEntity(
            nome = "Administrador",
            login = "admin",
            senhaHash = HashUtils.sha256("admin123"),
            cargo = "Administrador",
            email = "admin@saniblue.com.br"
        ),
        UsuarioEntity(
            nome = "Técnico Padrão",
            login = "tecnico",
            senhaHash = HashUtils.sha256("tecnico123"),
            cargo = "Técnico de Metrologia",
            email = "tecnico@saniblue.com.br"
        )
    )
}
