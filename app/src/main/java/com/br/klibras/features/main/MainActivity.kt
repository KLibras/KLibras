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
import com.br.klibras.features.account.AccountScreen
import com.br.klibras.features.account.ChangePasswordScreen
import com.br.klibras.features.account.ChangeUsernameScreen
import com.br.klibras.features.dex.DexScreen
import com.br.klibras.features.dex.Sign
import com.br.klibras.features.gesture.GestureLearningScreen
import com.br.klibras.features.learn.LearningScreen
import com.br.klibras.features.ranking.RankingScreen
import com.br.klibras.features.ranking.User
import com.br.klibras.shared.AppBottomNavigationBar

sealed class Screen(val route: String, val icon: Int? = null, val label: String? = null) {
    object Learning : Screen("learning_screen", R.drawable.aprenda_logo, "Aprenda")
    object Ranking : Screen("ranking_screen", R.drawable.ranking_logo, "Ranking")
    object Dex : Screen("dex_screen", R.drawable.dex_logo, "Dex")
    object Account : Screen("account_screen", R.drawable.conta_logo, "Conta")
    object GestureLearning : Screen("gesture_learning_screen")
    object ChangePassword : Screen("change_password_screen")
    object ChangeUsername : Screen("change_username_screen")
}

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

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarScreens = listOf(
        Screen.Learning.route,
        Screen.Ranking.route,
        Screen.Dex.route,
        Screen.Account.route
    )

    Scaffold(
        bottomBar = {
            if (currentRoute in bottomBarScreens) {
                AppBottomNavigationBar(navController = navController)
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Learning.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Learning.route) { LearningScreen(navController = navController) }

            composable(Screen.Ranking.route) {
                val mockUsers = listOf(
                    User("Maria", 150),
                    User("João", 125),
                    User("Ana", 110),
                    User("Você", 95),
                    User("Carlos", 80),
                    User("Carlos", 80),
                    User("Carlos", 80),
                    User("Carlos", 80)
                )
                RankingScreen(users = mockUsers)
            }
            composable(Screen.Dex.route) {
                val mockConqueredSigns = listOf(
                    Sign("Bom dia")
                )
                DexScreen(knownSigns = mockConqueredSigns)
            }

            composable(Screen.Account.route) {
                AccountScreen(
                    navController = navController,
                    username = "Username",
                    email = "email@email.com",
                    points = 100,
                    conqueredSigns = 1
                )
            }

            composable(Screen.ChangePassword.route) {
                ChangePasswordScreen(navController = navController)
            }

            composable(Screen.ChangeUsername.route) {
                ChangeUsernameScreen(navController = navController)
            }

            composable(
                route = "${Screen.GestureLearning.route}/{gestureName}",
                arguments = listOf(navArgument("gestureName") { type = NavType.StringType })
            ) {
                val gestureName = it.arguments?.getString("gestureName") ?: ""
                GestureLearningScreen(navController = navController, gestureName = gestureName)
            }
        }
    }
}