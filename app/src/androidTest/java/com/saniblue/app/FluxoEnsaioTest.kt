package com.saniblue.app

import androidx.compose.ui.test.hasAnySibling
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Testes de interface do fluxo principal do ensaio. Rodam num aparelho/emulador
 * real (gradlew connectedDebugAndroidTest), simulando toques e digitação.
 *
 * Cada teste é independente: entra pelo login (bypass de teste), abre o Novo
 * Ensaio e descarta o rascunho de execuções anteriores antes de começar.
 */
@RunWith(AndroidJUnit4::class)
class FluxoEnsaioTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    private val timeoutPadrao = 15_000L

    // ───────────────────────── helpers ─────────────────────────

    private fun aguardarTexto(texto: String, timeoutMs: Long = timeoutPadrao) {
        compose.waitUntil(timeoutMs) {
            // substring=true: os cards de resultado prefixam o texto com espaços (" APROVADO")
            // atLeastOneRootRequired=false: não falha se a Activity ainda está abrindo
            compose.onAllNodesWithText(texto, substring = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
    }

    /** Splash (~3s) → Login → ENTRAR (bypass) → Dashboard. */
    private fun entrarNoApp() {
        aguardarTexto("ENTRAR")
        compose.onNodeWithText("ENTRAR").performClick()
        aguardarTexto("Novo Ensaio")
    }

    /** Abre o Novo Ensaio pelo acesso rápido e garante formulário limpo. */
    private fun abrirNovoEnsaio() {
        compose.onAllNodesWithText("Novo Ensaio").onFirst().performClick()
        aguardarTexto("Norma do Ensaio")
        descartarRascunhoSePresente()
    }

    /**
     * O rascunho automático de uma execução anterior é restaurado de forma
     * assíncrona; espera um instante e, se o aviso aparecer, descarta.
     */
    private fun descartarRascunhoSePresente() {
        runCatching {
            compose.waitUntil(2_000) {
                compose.onAllNodesWithText("Descartar")
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            }
        }
        if (compose.onAllNodesWithText("Descartar").fetchSemanticsNodes().isNotEmpty()) {
            compose.onAllNodesWithText("Descartar").onFirst().performClick()
        }
        compose.waitForIdle()
    }

    /** Preenche todos os campos obrigatórios do cadastro (a Data já vem com a data atual). */
    private fun preencherCadastroMinimo() {
        // O nº de série também preenche a idade do hidrômetro automaticamente
        compose.onNodeWithText("Nº Hidrômetro *").performScrollTo().performTextInput("Y20B123456")
        compose.onNodeWithText("Nome da Companhia *").performScrollTo().performTextInput("Companhia Teste")
        compose.onNodeWithText("Matrícula *").performScrollTo().performTextInput("12345")
        compose.onNodeWithText("Cliente *").performScrollTo().performTextInput("Cliente Teste UI")
        compose.onNodeWithText("Endereço *").performScrollTo().performTextInput("Rua Teste, 100")
        compose.onNodeWithText("Bairro *").performScrollTo().performTextInput("Centro")
        compose.onNodeWithText("Cidade *").performScrollTo().performTextInput("Blumenau")
        compose.onNodeWithText("Temp. Água (°C) *").performScrollTo().performTextInput("20")
        compose.onNodeWithText("Técnico Responsável *").performScrollTo().performTextInput("Técnico UI")
        compose.onNodeWithText("Pressão Média (kg/cm²) *").performScrollTo().performTextInput("25")
    }

    /** Preenche uma das 3 medições da vazão em tela (índice 0..2). */
    private fun preencherMedicao(indice: Int, esc: String, ini: String, fin: String) {
        compose.onAllNodesWithText("Escoamento (L)")[indice].performScrollTo().performTextInput(esc)
        compose.onAllNodesWithText("Leit. Inicial")[indice].performScrollTo().performTextInput(ini)
        compose.onAllNodesWithText("Leit. Final")[indice].performScrollTo().performTextInput(fin)
    }

    private fun proximo() {
        compose.onNodeWithText("Próximo").performClick()
        compose.waitForIdle()
    }

    // ───────────────────────── testes ─────────────────────────

    /** Fluxo feliz: cadastro → medições da nominal aprovadas → salvar → detalhes. */
    @Test
    fun fluxoCompleto_preencheNominalESalva() {
        entrarNoApp()
        abrirNovoEnsaio()
        preencherCadastroMinimo()

        proximo() // → Nominal
        aguardarTexto("Medição 1")
        // Leituras contínuas (o hidrômetro não retrocede): 0→10, 10→20, 20→30
        preencherMedicao(0, "10", "0", "10")
        preencherMedicao(1, "10", "10", "20")
        preencherMedicao(2, "10", "20", "30")
        // Erro ~0% → vazão aprovada
        aguardarTexto("APROVADO")

        proximo() // → Transição
        proximo() // → Mínima
        proximo() // → Resultado
        aguardarTexto("RESULTADO FINAL DO ENSAIO")
        // Transição e mínima vazias → ensaio ainda pendente
        aguardarTexto("PENDENTE")

        compose.onNodeWithText("SALVAR").performClick()
        // Salvou e navegou para os detalhes do ensaio
        aguardarTexto("Ensaio Nº Y20B123456")
    }

    /** Regressão do crash: 'Ensaio não realizado' + navegar pelas etapas de vazão. */
    @Test
    fun ensaioNaoRealizado_navegaSemCrash() {
        entrarNoApp()
        abrirNovoEnsaio()

        // Liga o switch 'Ensaio não realizado' (fica no fim do cadastro)
        compose.onNode(isToggleable() and hasAnySibling(hasText("Ensaio não realizado")))
            .performScrollTo()
            .performClick()
        aguardarTexto("Motivo *")

        proximo() // → Nominal (antes crashava aqui)
        aguardarTexto("Ensaio marcado como não realizado — nenhuma medição é necessária.")
        proximo() // → Transição
        proximo() // → Mínima
        proximo() // → Resultado
        aguardarTexto("NÃO REALIZADO")
    }

    /** Leitura inicial menor que a final anterior pede confirmação ao sair do campo. */
    @Test
    fun leituraRetrocedida_mostraAlertaAoSairDoCampo() {
        entrarNoApp()
        abrirNovoEnsaio()

        proximo() // → Nominal (validação de cadastro não bloqueia a navegação)
        aguardarTexto("Medição 1")

        // M1 termina em 10; M2 começa em 9 (menor) → alerta ao perder o foco
        compose.onAllNodesWithText("Leit. Final")[0].performScrollTo().performTextInput("10")
        compose.onAllNodesWithText("Leit. Inicial")[1].performScrollTo().performTextInput("9")
        // Tira o foco do campo clicando em outro
        compose.onAllNodesWithText("Leit. Final")[1].performScrollTo().performClick()

        aguardarTexto("Confirmar leitura")
        compose.onNodeWithText("Revisar").performClick()
        compose.waitForIdle()
    }
}
