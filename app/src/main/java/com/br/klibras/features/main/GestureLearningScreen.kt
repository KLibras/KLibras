
package com.br.klibras.features.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

/**
 * Tela dedicada ao aprendizado de um gesto específico.
 * @param navController Controlador de navegação para gerenciar a transição entre telas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureLearningScreen(navController: NavController) {
    // Scaffold fornece a estrutura básica de layout do Material Design (TopAppBar, etc.).
    Scaffold(
        topBar = {
            // Barra de aplicativos no topo da tela.
            TopAppBar(
                title = { Text("Introdução", fontWeight = FontWeight.Bold) },
                // Ícone à esquerda para navegação (neste caso, para voltar).
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Ação de clique para voltar à tela anterior.
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Ícone de seta para a esquerda.
                            contentDescription = "Voltar"
                        )
                    }
                },
                // Define as cores da TopAppBar para se adaptar ao tema do app.
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background, // Cor de fundo.
                    titleContentColor = MaterialTheme.colorScheme.onBackground, // Cor do título.
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground // Cor do ícone de navegação.
                )
            )
        }
    ) { paddingValues -> // paddingValues contém o espaço ocupado pela TopAppBar.
        // Column organiza os elementos verticalmente.
        Column(
            modifier = Modifier
                .fillMaxSize() // Ocupa todo o espaço disponível.
                .padding(paddingValues) // Aplica o padding para não sobrepor a TopAppBar.
                .padding(16.dp), // Adiciona um espaçamento interno adicional.
            // Alinha o conteúdo da coluna no centro horizontal.
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Espaçador flexível para empurrar o conteúdo para baixo.
            Spacer(Modifier.weight(0.5f))

            // Box que serve como placeholder para o futuro modelo 3D.
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Ocupa toda a largura.
                    .weight(1f),      // Ocupa o espaço vertical principal.
                contentAlignment = Alignment.Center // Centraliza o texto do placeholder.
            ) {
                Text("Placeholder para Modelo 3D", color = Color.Gray)
            }

            // Espaçador flexível para criar um espaço entre o modelo 3D e o texto.
            Spacer(Modifier.weight(0.2f))

            // Texto descritivo sobre o aplicativo.
            Text(
                text = "O kLibras é seu professor de bolso para a Língua Brasileira de Sinais. Use a câmera do celular e a Inteligência Artificial para identificar sinais. Com ele, você aprende e registra seu progresso no vocabulário de Libras.",
                textAlign = TextAlign.Center, // Centraliza o alinhamento do texto.
            )

            // Espaçador flexível para empurrar os botões para a parte inferior.
            Spacer(Modifier.weight(0.5f))

            // Row para organizar os botões de navegação "Anterior" e "Próximo".
            Row(
                modifier = Modifier.fillMaxWidth(), // Ocupa toda a largura.
                // Distribui o espaço igualmente entre os botões.
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { /* TODO: Lógica para o botão Anterior */ },
                    modifier = Modifier.weight(1f) // Ocupa metade do espaço da Row.
                ) {
                    Text("Anterior")
                }
                // Espaçador horizontal entre os botões.
                Spacer(modifier = Modifier.width(16.dp))
                Button(
                    onClick = { /* TODO: Lógica para o botão Próximo */ },
                    modifier = Modifier.weight(1f), // Ocupa a outra metade do espaço da Row.
                    // Define a cor de fundo do botão para a cor de destaque.
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEBC32))
                ) {
                    Text("Próximo", color = Color.Black) // Cor do texto para contrastar com o fundo do botão.
                }
            }
        }
    }
}
