package com.saniblue.app.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Dashboard : Screen("dashboard")
    object ListaEnsaios : Screen("lista_ensaios")
    object NovoEnsaio : Screen("novo_ensaio")
    object DetalhesEnsaio : Screen("detalhes_ensaio/{ensaioId}") {
        fun createRoute(ensaioId: Long) = "detalhes_ensaio/$ensaioId"
    }
    object EditarEnsaio : Screen("editar_ensaio/{ensaioId}") {
        fun createRoute(ensaioId: Long) = "editar_ensaio/$ensaioId"
    }
    object CadastroHidrometros : Screen("cadastro_hidrometros")
    object Configuracoes : Screen("configuracoes")
}
