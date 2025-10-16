package com.br.klibras.features.gesture

import android.content.Context
import android.text.Spanned
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.RawResourceDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.br.klibras.core.service.api.Sign

/**
 * Tela dinâmica para aprendizado de gestos de um módulo buscado via API.
 *
 * @param navController Controlador de navegação.
 * @param moduleName O nome do módulo a ser buscado na API (ex: "introducao").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureLearningScreen(
    navController: NavController,
    moduleName: String,
    viewModel: GestureViewModel = viewModel()
) {
    LaunchedEffect(key1 = moduleName) {
        viewModel.fetchModule(moduleName)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = uiState.module?.name?.replaceFirstChar { it.uppercase() } ?: "Carregando..."
                    Text(title, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (uiState.module != null) {
                if (uiState.module!!.signs.isNotEmpty()) {
                    ModuleContent(
                        signs = uiState.module!!.signs
                    )
                } else {
                    Text(
                        text = "Este módulo ainda não possui sinais.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModuleContent(signs: List<Sign>) {
    var currentSignIndex by remember { mutableIntStateOf(0) }
    val currentSign = signs[currentSignIndex]
    val context = LocalContext.current

    val videoResId = remember(currentSign.videoUrl) {
        getVideoResId(context, currentSign.videoUrl)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = currentSign.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            if (videoResId != 0) {
                VideoPlayer(videoResId = videoResId)
            } else {
                Text("Vídeo não encontrado para ${currentSign.videoUrl}")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        HtmlText(
            html = currentSign.desc,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .weight(1f) // Faz a descrição ocupar o espaço disponível
        )

        if (videoResId != 0) {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { /* TODO: Navegar para câmera com `currentSign.name` como parâmetro */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEBC32))
            ) {
                Icon(Icons.Default.CameraAlt, "Praticar", tint = Color.Black)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Praticar", color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { currentSignIndex-- },
                modifier = Modifier.weight(1f),
                enabled = currentSignIndex > 0
            ) {
                Text("Anterior")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { currentSignIndex++ },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEBC32)),
                enabled = currentSignIndex < signs.lastIndex
            ) {
                Text("Próximo", color = Color.Black)
            }
        }
    }
}

private fun getVideoResId(context: Context, videoName: String): Int {
    return context.resources.getIdentifier(videoName, "raw", context.packageName)
}

@Composable
fun HtmlText(html: String, modifier: Modifier = Modifier) {
    val spanned: Spanned = remember(html) {
        HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_LEGACY)
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            android.widget.TextView(context).apply {
                text = spanned
                textSize = 16f
                textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
                // A cor será herdada do tema, mas pode ser forçada se necessário
            }
        },
        update = {
            it.text = spanned
        }
    )
}

/**
 * Um Composable que exibe um vídeo a partir de um recurso `raw` e gerencia seu ciclo de vida.
 *
 * @param videoResId O ID do recurso de vídeo na pasta `res/raw` (ex: R.raw.meu_video).
 */
@androidx.annotation.OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(modifier: Modifier = Modifier, videoResId: Int) {
    val context = LocalContext.current

    val exoPlayer = remember(videoResId) {
        ExoPlayer.Builder(context).build().apply {
            val uri = RawResourceDataSource.buildRawResourceUri(videoResId)
            val mediaItem = MediaItem.fromUri(uri)
            setMediaItem(mediaItem)

            repeatMode = ExoPlayer.REPEAT_MODE_ONE
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            PlayerView(it).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            }
        }
    )
}