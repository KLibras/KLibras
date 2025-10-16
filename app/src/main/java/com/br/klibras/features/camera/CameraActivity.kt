package com.br.klibras.features.camera

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.br.klibras.R
import com.br.klibras.core.ui.theme.Green100
import com.br.klibras.core.ui.theme.Grey70
import com.br.klibras.core.ui.theme.HighlightYellow
import com.br.klibras.core.ui.theme.KLibrasTheme
import com.br.klibras.core.ui.theme.Red100

class CameraActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val signName = intent.getStringExtra("SIGN_NAME_EXTRA") ?: "Praticar"

        setContent {
            KLibrasTheme(dynamicColor = false) {
                CameraScreen(signName = signName)
            }
        }
    }
}

enum class ScreenState {
    Camera,
    ResultA,
    ResultB
}

@Composable
fun RecordButton(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 75.dp, height = 55.dp)
                .background(
                    color = HighlightYellow,
                    shape = RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.camera_logo),
                contentDescription = "Gravar",
                modifier = Modifier.size(35.dp),
                tint = Grey70
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Gravar",
            fontSize = 20.sp,
            color = Color.Black
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraLayout(
    signName: String,
    onRecordClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    var hasPermissions by remember {
        mutableStateOf(
            permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { grantedPermissions ->
            hasPermissions = grantedPermissions.values.all { it }
        }
    )
    LaunchedEffect(key1 = Unit) {
        if (!hasPermissions) {
            launcher.launch(permissions)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = signName, fontWeight = FontWeight.Bold, color = Color.White)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = HighlightYellow
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                if (hasPermissions) {
                    CameraPreview(lifecycleOwner = lifecycleOwner)
                } else {
                    PermissionDeniedScreen {
                        launcher.launch(permissions)
                    }
                }
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(
                            onClick = onRecordClick,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 75.dp, height = 55.dp)
                            .background(
                                color = HighlightYellow,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.camera_logo),
                            contentDescription = "Gravar",
                            modifier = Modifier.size(35.dp),
                            tint = Grey70
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gravar",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }
        }
    )
}

@Composable
fun CameraPreview(lifecycleOwner: LifecycleOwner) {
    val context = LocalContext.current
    val cameraController = remember { LifecycleCameraController(context) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                this.controller = cameraController
                cameraController.bindToLifecycle(lifecycleOwner)
                cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun PermissionDeniedScreen(onRequestPermission: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Permissão da câmera e áudio necessária",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Este recurso precisa de acesso à sua câmera e microfone para gravar o sinal. Por favor, conceda a permissão.",
            textAlign = TextAlign.Center,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRequestPermission) {
            Text("Conceder Permissão")
        }
    }
}

@Composable
fun ResultRight(onBackClick: () -> Unit, onNavigateToResultB: () -> Unit) {
    val scale = remember { Animatable(0.3f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.5f,
                stiffness = 100f
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Green100),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Parabéns",
                fontSize = 40.sp,
                color = Color.Black
            )
            Text(
                text = "+ 5 pontos",
                fontSize = 20.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(50.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(50.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Checkmark",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(60.dp)
                        .scale(scale.value)
                )
            }
            Spacer(modifier = Modifier.height(50.dp))
            Text(
                text = "Sinal realizado corretamente",
                fontSize = 22.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(80.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .clickable(
                        onClick = onNavigateToResultB,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .padding(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 60.dp, height = 40.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Próximo sinal",
                        modifier = Modifier.size(30.dp),
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Próximo sinal",
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
fun ResultWrong(onBackClick: () -> Unit, onNavigateToResultB: () -> Unit) {
    val scale = remember { Animatable(0.3f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = 0.5f,
                stiffness = 100f
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Red100),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Incorreto",
                fontSize = 40.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(50.dp))
                    .border(2.dp, Color.Black, RoundedCornerShape(50.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Cancel,
                    contentDescription = "Incorrect",
                    tint = Color.Black,
                    modifier = Modifier
                        .size(60.dp)
                        .scale(scale.value)
                )
            }
            Spacer(modifier = Modifier.height(50.dp))
            Text(
                text = "Sinal não reconhecido",
                fontSize = 22.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(80.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(
                            onClick = onBackClick,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 60.dp, height = 40.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.camera_logo),
                            contentDescription = "Tentar novamente",
                            modifier = Modifier.size(30.dp),
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tentar novamente",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .clickable(
                            onClick = onNavigateToResultB,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 60.dp, height = 40.dp)
                            .background(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Próximo sinal",
                            modifier = Modifier.size(30.dp),
                            tint = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Próximo sinal",
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun CameraScreen(signName: String) {
    var currentScreen by remember { mutableStateOf(ScreenState.Camera) }
    val context = LocalContext.current

    when (currentScreen) {
        ScreenState.Camera -> {
            CameraLayout(
                signName = signName,
                onRecordClick = {
                    val isCorrect = listOf(true, false).random()
                    currentScreen = if (isCorrect) ScreenState.ResultA else ScreenState.ResultB
                },
                onBackClick = {
                    (context as? Activity)?.finish()
                }
            )
        }
        ScreenState.ResultA -> {
            ResultRight(
                onBackClick = { currentScreen = ScreenState.Camera },
                onNavigateToResultB = {
                    (context as? Activity)?.finish()
                }
            )
        }
        ScreenState.ResultB -> {
            ResultWrong(
                onBackClick = { currentScreen = ScreenState.Camera },
                onNavigateToResultB = {
                    (context as? Activity)?.finish()
                }
            )
        }
    }
}