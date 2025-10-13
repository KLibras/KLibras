package com.br.klibras.features.gesture

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.br.klibras.R
import com.br.klibras.features.main.Screen

// Lista ordenada de todos os gestos, que define a sequência de navegação.
private val gestureFlow = listOf(
    "introducao",
    "bom_dia",
    "tudo_bem",
    "qual_seu_nome",
    "obrigado",
    "em_preparacao"
)

/**
 * Tela dinâmica para o aprendizado de um gesto, exibindo um vídeo e informações.
 *
 * Esta tela é responsável por:
 * 1. Receber o nome de um gesto via navegação (ex: "bom_dia").
 * 2. Mapear esse nome para o conteúdo correto (título, vídeo, descrição).
 * 3. Exibir o vídeo do gesto (se existir).
 * 4. Exibir a descrição do gesto.
 * 5. Fornecer botões para navegar para o gesto anterior e o próximo na sequência.
 *
 * @param navController Controlador de navegação para gerenciar as transições entre telas.
 * @param gestureName O identificador único do gesto a ser exibido.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureLearningScreen(navController: NavController, gestureName: String) {

    // Bloco 'when' que funciona como um banco de dados dinâmico para a tela.
    // Ele retorna um Triple contendo o título, o ID do vídeo e a descrição
    // com base no 'gestureName' recebido.
    val (title, videoResId, description) = when (gestureName) {
        "introducao" -> Triple("Introdução", 0, "O kLibras é seu professor de bolso para a Língua Brasileira de Sinais. Use a câmera do celular e a Inteligência Artificial para identificar sinais. Com ele, você aprende e registra seu progresso no vocabulário de Libras.")
        "bom_dia" -> Triple("Bom dia", R.raw.bom_dia, "\"Bom Dia\" é a saudação utilizada para o período matinal. O sinal é composto por duas partes: 'BOM' e 'DIA'. Para 'BOM', inicie com a mão fechada próxima à boca e abra-a rapidamente para frente. Para 'DIA', use a configuração de mão em 'D' (indicador esticado) e toque a ponta do indicador na têmpora, elevando-a para cima em seguida, simulando o nascer do sol.")
        "tudo_bem" -> Triple("Tudo bem?", R.raw.tudo_bem, "Este é um cumprimento utilizado para questionar o bem-estar de alguém. Para sinalizar \"Tudo Bem?\", gire a mão aberta (ou em configuração de \"joia\", com o polegar esticado) para frente e para trás, na altura do peito, ou use o sinal de 'BOM' seguido pelo sinal de 'JOIA'. É essencial acompanhar o sinal com a expressão facial de pergunta (sobrancelhas levemente levantadas).")
        "qual_seu_nome" -> Triple("Qual seu nome?", R.raw.qual_seu_nome, "Para perguntar o nome de alguém, utilize a configuração de mão em \"U\" (indicador e médio esticados e unidos). Toque a ponta desses dedos no peito duas vezes. Este é o sinal de 'NOME'. Na sequência, aponte a mão aberta na direção da pessoa (sinal de 'SEU'), mantendo a expressão facial de pergunta. O sinal combina 'NOME' + 'SEU' com a expressão de questionamento.")
        "obrigado" -> Triple("Obrigado", R.raw.obrigado, "O sinal de \"Obrigado\" expressa gratidão e é neutro em relação ao gênero. A configuração de mão é aberta, com os dedos unidos. Leve a mão aberta até o queixo e projete-a levemente para frente e para cima, na direção do interlocutor, com um movimento breve e decidido. O agradecimento pode ser reforçado com uma expressão facial de contentamento.")
        // Caso padrão para gestos futuros ou não mapeados.
        else -> Triple("Em preparação", 0, "Estamos precisando de orçamento pra isso.")
    }

    // Encontra a posição (índice) do gesto atual na lista de fluxo para a navegação "Próximo/Anterior".
    val currentIndex = gestureFlow.indexOf(gestureName)

    // Scaffold fornece a estrutura de layout principal (barra superior, conteúdo, etc.).
    Scaffold(
        topBar = {
            // Barra de aplicativos no topo da tela.
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) }, // Título dinâmico.
                navigationIcon = {
                    // Ícone de "voltar" que fecha a tela atual.
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                // Cores que se adaptam ao tema do app (claro/escuro).
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues -> // Contém o espaçamento para não sobrepor o conteúdo da TopAppBar.
        // Column principal que organiza o conteúdo verticalmente.
        Column(
            modifier = Modifier
                .fillMaxSize() // Ocupa todo o espaço.
                .padding(paddingValues) // Aplica o espaçamento da TopAppBar.
                .padding(16.dp), // Adiciona uma margem interna.
            horizontalAlignment = Alignment.CenterHorizontally // Centraliza tudo horizontalmente.
        ) {
            // Espaçador para dar uma margem no topo da tela.
            Spacer(modifier = Modifier.height(32.dp))

            // Box que contém o player de vídeo ou fica vazio.
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Ocupa toda a largura.
                    .height(250.dp), // Altura fixa para o player de vídeo.
                contentAlignment = Alignment.Center // Centraliza o conteúdo (player).
            ) {
                // Só exibe o player se houver um ID de vídeo válido (diferente de 0).
                if (videoResId != 0) {
                    VideoPlayer(videoResId = videoResId)
                }
            }

            // Espaçador vertical entre o vídeo e a descrição.
            Spacer(modifier = Modifier.height(24.dp))

            // Texto descritivo do gesto.
            Text(
                text = description, // Usa a descrição dinâmica do bloco 'when'.
                textAlign = TextAlign.Center, // Alinha o texto no centro.
                modifier = Modifier.padding(horizontal = 16.dp) // Evita que o texto encoste nas bordas.
            )

            // O botão de praticar só aparece se houver um vídeo associado ao gesto.
            if (videoResId != 0) {
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { /* TODO: Implementar navegação para a câmera */ },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEBC32)) // Fundo amarelo.
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Praticar com a câmera",
                        tint = Color.Black // Ícone preto para contrastar com o fundo.
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Praticar", color = Color.Black)
                }
            }

            // Espaçador flexível que "empurra" os botões de navegação para o final da tela.
            Spacer(Modifier.weight(1f))

            // Botões de navegação "Anterior" e "Próximo".
            Row(
                modifier = Modifier.fillMaxWidth(), // Ocupa toda a largura.
                horizontalArrangement = Arrangement.SpaceBetween // Deixa um espaço entre os botões.
            ) {
                // Botão "Anterior".
                Button(
                    onClick = {
                        val previousGestureName = gestureFlow[currentIndex - 1]
                        // Navega para o gesto anterior, substituindo a tela atual.
                        navController.navigate("${Screen.GestureLearning.route}/$previousGestureName") {
                            popUpTo("${Screen.GestureLearning.route}/{gestureName}") { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f), // Ocupa metade do espaço.
                    enabled = currentIndex > 0 // Desabilitado se for o primeiro item da lista.
                ) {
                    Text("Anterior")
                }
                Spacer(modifier = Modifier.width(16.dp))

                // Botão "Próximo".
                Button(
                    onClick = {
                        val nextGestureName = gestureFlow[currentIndex + 1]
                        // Navega para o próximo gesto, substituindo a tela atual.
                        navController.navigate("${Screen.GestureLearning.route}/$nextGestureName") {
                            popUpTo("${Screen.GestureLearning.route}/{gestureName}") { inclusive = true }
                        }
                    },
                    modifier = Modifier.weight(1f), // Ocupa a outra metade.
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEBC32)), // Fundo amarelo.
                    enabled = currentIndex < gestureFlow.lastIndex // Desabilitado se for o último item.
                ) {
                    Text("Próximo", color = Color.Black)
                }
            }
        }
    }
}


/**
 * Um Composable que exibe um vídeo a partir de um recurso `raw` e gerencia seu ciclo de vida.
 *
 * Este componente faz o seguinte:
 * 1. Cria uma instância do ExoPlayer.
 * 2. Recria o player se o ID do vídeo mudar (útil para a navegação "Próximo"/"Anterior").
 * 3. Garante que os recursos do player sejam liberados (`release()`) quando a tela é descartada,
 *    evitando vazamentos de memória.
 *
 * @param videoResId O ID do recurso de vídeo na pasta `res/raw` (ex: R.raw.meu_video).
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(modifier: Modifier = Modifier, videoResId: Int) {
    val context = LocalContext.current

    // 'remember' com uma chave (videoResId) garante que o ExoPlayer seja recriado
    // sempre que o vídeo a ser exibido mudar.
    val exoPlayer = remember(videoResId) {
        ExoPlayer.Builder(context).build().apply {
            // Constrói a URI para encontrar o arquivo de vídeo na pasta 'res/raw'.
            val uri = RawResourceDataSource.buildRawResourceUri(videoResId)
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)

            // Configurações do player.
            repeatMode = ExoPlayer.REPEAT_MODE_ONE // Faz o vídeo repetir em loop.
            playWhenReady = true // Começa a tocar assim que o buffer estiver pronto.
            prepare() // Prepara o player para a reprodução.
        }
    }

    // 'DisposableEffect' é usado para lidar com "efeitos colaterais" que precisam ser
    // limpos quando o Composable sai da tela. É ideal para liberar recursos como players.
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release() // Libera os recursos do ExoPlayer para evitar vazamentos de memória.
        }
    }

    // `AndroidView` é o componente usado para hospedar uma View do Android tradicional
    // dentro de um layout do Jetpack Compose.
    AndroidView(
        modifier = modifier,
        factory = {
            // Cria a PlayerView, que é a UI visual do ExoPlayer.
            PlayerView(it).apply {
                player = exoPlayer // Associa a View ao nosso player.
                useController = false // Esconde os controles de UI (play, pause, barra de progresso, etc.).
                // Define como o vídeo deve se ajustar. RESIZE_MODE_FIT garante que o vídeo
                // inteiro seja visível, mantendo a proporção, sem cortar as bordas.
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }
    )
}
