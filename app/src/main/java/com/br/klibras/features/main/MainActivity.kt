
package com.br.klibras.features.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.br.klibras.R
import com.br.klibras.core.ui.theme.KLibrasTheme
import com.br.klibras.features.gesture.GestureLearningScreen
import com.br.klibras.features.learn.LearningScreen
import com.br.klibras.shared.AppBottomNavigationBar

/**
 * Define as diferentes telas do aplicativo para o sistema de navegação.
 * @param route A rota de navegação única para a tela.
 * @param icon O recurso do ícone (opcional), usado na barra de navegação inferior.
 * @param label O nome da tela (opcional), usado na barra de navegação inferior.
 */
sealed class Screen(val route: String, val icon: Int? = null, val label: String? = null) {
    object Learning : Screen("learning_screen", R.drawable.aprenda_logo, "Aprenda")
    object Ranking : Screen("ranking_screen", R.drawable.ranking_logo, "Ranking")
    object Dex : Screen("dex_screen", R.drawable.dex_logo, "Dex")
    object Account : Screen("account_screen", R.drawable.conta_logo, "Conta")
    object GestureLearning : Screen("gesture_learning_screen")
}

/**
 * A Activity principal que hospeda os Composables da navegação principal do app.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KLibrasTheme {
                MainScreen()
            }
        }
    }
}

/**
 * O Composable principal que configura a estrutura da tela, incluindo a barra de navegação
 * e o host de navegação que gerencia a troca de telas.
 */
@Composable
fun MainScreen() {
    // Cria e lembra o controlador de navegação para todo o escopo do MainScreen.
    val navController = rememberNavController()
    // Observa a pilha de navegação para obter a rota da tela atual.
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Lista de rotas que devem exibir a barra de navegação inferior.
    val bottomBarScreens = listOf(
        Screen.Learning.route,
        Screen.Ranking.route,
        Screen.Dex.route,
        Screen.Account.route
    )

    // Scaffold fornece a estrutura de layout com slots para TopBar, BottomBar, etc.
    Scaffold(
        // Define o conteúdo da barra de navegação inferior.
        bottomBar = {
            // A barra só é exibida se a rota atual estiver na lista 'bottomBarScreens'.
            if (currentRoute in bottomBarScreens) {
                AppBottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues -> // paddingValues contém os espaçamentos necessários para a barra de navegação.
        // NavHost é o contêiner que exibe o destino de navegação atual.
        NavHost(
            navController = navController,
            startDestination = Screen.Learning.route, // A primeira tela a ser exibida.
            // Aplica o padding para que o conteúdo da tela não fique sob a barra de navegação.
            modifier = Modifier.padding(paddingValues)
        ) {
            // Define o Composable para cada rota de navegação.
            composable(Screen.Learning.route) { LearningScreen(navController = navController) }
            composable(Screen.Ranking.route) { Text("Ranking Screen") } // Placeholder
            composable(Screen.Dex.route) { Text("Dex Screen") } // Placeholder
            composable(Screen.Account.route) { Text("Account Screen") } // Placeholder
            
            // Define a rota para a tela de aprendizado, que agora aceita um argumento {gestureName}.
            composable(
                route = "${Screen.GestureLearning.route}/{gestureName}",
                arguments = listOf(navArgument("gestureName") { type = NavType.StringType })
            ) {
                // Extrai o argumento da rota e o passa para a tela.
                val gestureName = it.arguments?.getString("gestureName") ?: ""
                GestureLearningScreen(navController = navController, gestureName = gestureName)
            }
        }
    }
}
