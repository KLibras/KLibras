
package com.br.klibras.features.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState

/**
 * A barra de navegação inferior principal do aplicativo.
 * @param navController O controlador de navegação usado para navegar entre as telas.
 */
@Composable
fun AppBottomNavigationBar(navController: NavController) {
    // Lista de telas que aparecerão na barra de navegação.
    val items = listOf(
        Screen.Learning,
        Screen.Ranking,
        Screen.Dex,
        Screen.Account,
    )

    // Componente BottomAppBar que serve como contêiner para a barra de navegação.
    BottomAppBar(
        // Cor de fundo baseada no tema do aplicativo.
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        // Obtém o estado atual da pilha de navegação para saber qual tela está visível.
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        // Row para organizar os itens da barra de navegação horizontalmente.
        Row(
            modifier = Modifier.fillMaxWidth(),
            // Distribui os itens uniformemente ao longo da largura da barra.
            horizontalArrangement = Arrangement.SpaceAround,
            // Centraliza os itens verticalmente.
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Itera sobre a lista de telas e cria um BottomNavItem para cada uma.
            items.forEach { screen ->
                BottomNavItem(
                    screen = screen,
                    // O item é considerado "selecionado" se sua rota corresponde à rota atual na hierarquia de navegação.
                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                    onClick = {
                        // Navega para a rota da tela clicada.
                        if (screen.route != currentDestination?.route) {
                            navController.navigate(screen.route) {
                                // Evita empilhar a mesma tela várias vezes.
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true // Garante uma única instância da tela.
                                restoreState = true // Restaura o estado ao voltar para a tela.
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Representa um único item (ícone e texto) na barra de navegação.
 * @param screen O objeto Screen que contém os dados do item (rota, ícone, texto).
 * @param selected Verdadeiro se o item estiver selecionado, falso caso contrário.
 * @param onClick Ação a ser executada quando o item é clicado.
 */
@Composable
private fun RowScope.BottomNavItem(
    screen: Screen,
    selected: Boolean,
    onClick: () -> Unit
) {
    // Column organiza o ícone e o texto verticalmente.
    Column(
        modifier = Modifier
            .weight(1f) // Faz com que cada item ocupe o mesmo espaço horizontal.
            .clickable(onClick = onClick) // Torna a área do item clicável.
            .padding(vertical = 8.dp), // Espaçamento vertical interno.
        horizontalAlignment = Alignment.CenterHorizontally, // Centraliza o ícone e o texto horizontalmente.
        verticalArrangement = Arrangement.Center // Centraliza o conteúdo verticalmente.
    ) {
        // A cor é amarela se o item estiver selecionado, caso contrário, usa a cor padrão do tema.
        val color = if (selected) Color(0xFFDEBC32) else MaterialTheme.colorScheme.onBackground

        // Garante que o ícone e o texto só sejam exibidos se não forem nulos.
        if (screen.icon != null && screen.label != null) {
            Icon(
                painter = painterResource(id = screen.icon),
                contentDescription = screen.label,
                tint = color, // Aplica a cor selecionada (amarela) ou padrão.
                modifier = Modifier.size(24.dp) // Tamanho do ícone.
            )
            Text(
                text = screen.label,
                color = color, // Aplica a cor selecionada (amarela) ou padrão.
                fontSize = 18.sp // Tamanho da fonte do texto.
            )
        }
    }
}
