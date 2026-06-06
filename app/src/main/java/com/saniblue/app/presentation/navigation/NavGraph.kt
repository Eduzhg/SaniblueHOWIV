package com.saniblue.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.saniblue.app.presentation.screens.configuracoes.ConfiguracoesScreen
import com.saniblue.app.presentation.screens.dashboard.DashboardScreen
import com.saniblue.app.presentation.screens.detalhes.DetalhesEnsaioScreen
import com.saniblue.app.presentation.screens.ensaios.ListaEnsaiosScreen
import com.saniblue.app.presentation.screens.hidrometros.CadastroHidrometrosScreen
import com.saniblue.app.presentation.screens.login.LoginScreen
import com.saniblue.app.presentation.screens.novo_ensaio.NovoEnsaioScreen
import com.saniblue.app.presentation.screens.splash.SplashScreen

@Composable
fun SaniblueNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToNovoEnsaio = { navController.navigate(Screen.NovoEnsaio.route) },
                onNavigateToListaEnsaios = { navController.navigate(Screen.ListaEnsaios.route) },
                onNavigateToConfiguracoes = { navController.navigate(Screen.Configuracoes.route) },
                onNavigateToCadastroHidrometros = { navController.navigate(Screen.CadastroHidrometros.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ListaEnsaios.route) {
            ListaEnsaiosScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToDetalhes = { id ->
                    navController.navigate(Screen.DetalhesEnsaio.createRoute(id))
                },
                onNavigateToNovoEnsaio = { navController.navigate(Screen.NovoEnsaio.route) }
            )
        }

        composable(Screen.NovoEnsaio.route) {
            NovoEnsaioScreen(
                onNavigateBack = { navController.popBackStack() },
                onEnsaioSalvo = { id ->
                    navController.navigate(Screen.DetalhesEnsaio.createRoute(id)) {
                        popUpTo(Screen.NovoEnsaio.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.DetalhesEnsaio.route,
            arguments = listOf(navArgument("ensaioId") { type = NavType.LongType })
        ) { backStackEntry ->
            val ensaioId = backStackEntry.arguments?.getLong("ensaioId") ?: 0L
            DetalhesEnsaioScreen(
                ensaioId = ensaioId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditar = { id ->
                    navController.navigate(Screen.EditarEnsaio.createRoute(id))
                }
            )
        }

        composable(
            route = Screen.EditarEnsaio.route,
            arguments = listOf(navArgument("ensaioId") { type = NavType.LongType })
        ) { backStackEntry ->
            val ensaioId = backStackEntry.arguments?.getLong("ensaioId") ?: 0L
            NovoEnsaioScreen(
                ensaioId = ensaioId,
                onNavigateBack = { navController.popBackStack() },
                onEnsaioSalvo = { _ -> navController.popBackStack() }
            )
        }

        composable(Screen.CadastroHidrometros.route) {
            CadastroHidrometrosScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Configuracoes.route) {
            ConfiguracoesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
