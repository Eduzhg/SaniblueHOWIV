package com.saniblue.app.data.local.database

import com.saniblue.app.data.local.entity.HidrometroModeloEntity
import com.saniblue.app.data.local.entity.UsuarioEntity
import com.saniblue.app.domain.model.NormaEnsaio
import com.saniblue.app.util.HashUtils

/**
 * Dados iniciais inseridos no primeiro uso do app.
 *
 * O catálogo de hidrômetros é FIXO (não é criado à mão pelo técnico): a norma e as
 * vazões de referência são resolvidas automaticamente a partir do nº de série do
 * ensaio. Ver domain/model/HidrometroModelo.kt e util/Extensions.kt (detecção do
 * formato do serial). Para adicionar uma nova capacidade, basta editar esta lista.
 */
object DatabasePrePopulate {

    fun getModelosHidrometro(): List<HidrometroModeloEntity> {
        val p246 = NormaEnsaio.PORTARIA_246.name
        val p155 = NormaEnsaio.PORTARIA_155.name

        // Portaria 246: só 2 capacidades conhecidas hoje, identificadas pela 1ª letra
        // (nome sempre em L/h — nunca m³/h, mesma convenção usada em toda a tela)
        val modelos246 = listOf(
            HidrometroModeloEntity(
                nome = "Hidrômetro 750 L/h (Y) — Portaria 246",
                descricao = "1ª letra do nº de série: Y",
                norma = p246, letra = "Y", classeR = "",
                vazaoNominal = 750.0, vazaoTransicao = 60.0, vazaoMinima = 15.0
            ),
            HidrometroModeloEntity(
                nome = "Hidrômetro 1500 L/h (A) — Portaria 246",
                descricao = "1ª letra do nº de série: A",
                norma = p246, letra = "A", classeR = "",
                vazaoNominal = 1500.0, vazaoTransicao = 120.0, vazaoMinima = 60.0
            )
        )

        // Portaria 155: a Vazão Nominal (Q3) é fixa pela letra; a Transição (Q2) e a
        // Mínima (Q1) variam pela classe metrológica (R80/R100/R125) do hidrômetro,
        // que o técnico identifica visualmente em campo (não vem no nº de série).
        data class Q3PorLetra(val letra: String, val vazaoNominal: Double)
        data class LimitesPorClasse(val classe: String, val vazaoTransicao: Double, val vazaoMinima: Double)

        val q3PorLetra = listOf(
            Q3PorLetra("Y", 1000.0),
            Q3PorLetra("Z", 1600.0),
            Q3PorLetra("A", 2500.0)
        )
        val limitesPorLetra = mapOf(
            "Y" to listOf(
                LimitesPorClasse("R80", 20.0, 12.5),
                LimitesPorClasse("R100", 16.0, 10.0),
                LimitesPorClasse("R125", 12.8, 8.0)
            ),
            "Z" to listOf(
                LimitesPorClasse("R80", 32.0, 20.0),
                LimitesPorClasse("R100", 25.6, 16.0),
                LimitesPorClasse("R125", 20.48, 12.8)
            ),
            "A" to listOf(
                LimitesPorClasse("R80", 50.0, 31.25),
                LimitesPorClasse("R100", 40.0, 25.0),
                LimitesPorClasse("R125", 32.0, 20.0)
            )
        )

        val modelos155 = q3PorLetra.flatMap { cap ->
            limitesPorLetra.getValue(cap.letra).map { lim ->
                HidrometroModeloEntity(
                    nome = "Hidrômetro ${cap.vazaoNominal.toInt()} L/h (${cap.letra}) — ${lim.classe} — Portaria 155",
                    descricao = "1ª letra do nº de série: ${cap.letra} — classe ${lim.classe}",
                    norma = p155, letra = cap.letra, classeR = lim.classe,
                    vazaoNominal = cap.vazaoNominal,
                    vazaoTransicao = lim.vazaoTransicao,
                    vazaoMinima = lim.vazaoMinima
                )
            }
        }

        return modelos246 + modelos155
    }

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
