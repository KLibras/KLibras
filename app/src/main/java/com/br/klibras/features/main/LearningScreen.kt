
package com.br.klibras.features.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.br.klibras.R

/**
 * A tela principal de aprendizado, que exibe os módulos como uma árvore.
 */
@Composable
fun LearningScreen(navController: NavController) {
    // Column principal que permite rolagem vertical.
    Column(
        modifier = Modifier
            .fillMaxSize() // Ocupa toda a altura e largura da tela.
            .background(MaterialTheme.colorScheme.background) // Define a cor de fundo com base no tema.
            .verticalScroll(rememberScrollState()) // Adiciona a capacidade de rolagem.
            .padding(16.dp), // Adiciona um espaçamento interno de 16.dp em todos os lados.
        
        // Alinha todos os filhos (o conteúdo) no centro horizontal da tela.
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Espaçador manual no topo para ajustar a posição inicial dos elementos.
        // Altere este valor para mover o conteúdo para cima ou para baixo.
        Spacer(modifier = Modifier.height(80.dp))

        // Seção que contém a árvore de módulos de aprendizado.
        TreeSection(navController = navController)

        // Você pode adicionar mais itens aqui para testar a rolagem no futuro.
    }
}

/**
 * Composable que desenha a árvore de módulos.
 */
@Composable
fun TreeSection(navController: NavController) {
    Column(
        // Alinha o conteúdo (Nó raiz e a Row de nós filhos) no centro horizontal.
        horizontalAlignment = Alignment.CenterHorizontally,
        // Adiciona um espaçamento vertical de 16.dp entre cada filho direto (NodeItem, Spacer, Row).
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Nó raiz da árvore.
        NodeItem(
            icon = R.drawable.introducao_logo, 
            label = "Introdução",
            // Define a ação de clique para navegar para a tela de aprendizado de gestos.
            onClick = { navController.navigate(Screen.GestureLearning.route) }
        )
        
        // Espaçador para criar a linha de conexão visual entre o nó pai e os filhos.
        Spacer(modifier = Modifier.height(32.dp))

        // Linha que organiza os nós filhos lado a lado.
        Row(
            // Adiciona um espaçamento horizontal de 64.dp entre os nós filhos.
            horizontalArrangement = Arrangement.spacedBy(64.dp)
        ) {
            NodeItem(icon = R.drawable.em_preparacao_logo, label = "Em preparação")
            NodeItem(icon = R.drawable.em_preparacao_logo, label = "Em preparação")
        }
    }
}

/**
 * Representa um único nó (módulo) na árvore de aprendizado.
 * @param icon O recurso do ícone para o nó.
 * @param label O texto descritivo do nó.
 * @param onClick Ação a ser executada quando o nó é clicado. Se for nulo, o nó não será clicável.
 */
@Composable
fun NodeItem(icon: Int, label: String, onClick: (() -> Unit)? = null) {
    Column(
        // Alinha o ícone e o texto do nó no centro horizontal.
        horizontalAlignment = Alignment.CenterHorizontally,
        // Torna a coluna clicável se uma função onClick for fornecida.
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    ) {
        // Box que desenha o círculo de fundo para o ícone.
        Box(
            modifier = Modifier
                .size(80.dp) // Define um tamanho fixo de 80x80 dp.
                .clip(CircleShape) // Corta o Box em um formato de círculo.
                .background(Color(0xFFDEBC32)), // Cor de fundo amarela.
            // Alinha o conteúdo (a imagem) no centro do Box.
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.size(48.dp) // Define o tamanho do ícone dentro do círculo.
            )
        }
        // Espaçador vertical entre o círculo e o texto.
        Spacer(modifier = Modifier.height(8.dp))
        // Texto do nó.
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground, // Cor do texto baseada no tema.
            fontWeight = FontWeight.Bold, // Define a fonte como negrito.
            fontSize = 16.sp // Define o tamanho da fonte.
        )
    }
}
