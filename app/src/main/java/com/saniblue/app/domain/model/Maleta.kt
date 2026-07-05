package com.saniblue.app.domain.model

/**
 * Maleta de verificação (kit do técnico) contendo o hidrômetro padrão ultrassônico.
 *
 * Cada maleta vem com um certificado de fábrica informando o **erro padrão** do
 * seu hidrômetro ultrassônico (valor fixo, geralmente pequeno). Esse erro impacta
 * o cálculo: o volume escoado real é corrigido por
 *   escoamentoCorrigido = escoamento * (100 - erroPadrao) / 100
 *
 * Por enquanto a lista é fixa (uma maleta de teste). No futuro vira um cadastro
 * (tabela Room) para o usuário registrar as maletas reais com seus certificados.
 */
data class Maleta(
    val id: String,
    val nome: String,
    val erroPadrao: Double // % (ex.: 0.5)
)
