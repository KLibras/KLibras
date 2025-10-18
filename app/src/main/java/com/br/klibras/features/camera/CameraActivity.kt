package com.br.klibras.features.camera

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.camera.view.video.AudioConfig
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.LifecycleOwner
import com.br.klibras.R
import com.br.klibras.core.ui.theme.Grey70
import com.br.klibras.core.ui.theme.HighlightYellow
import com.br.klibras.core.ui.theme.KLibrasTheme
import com.br.klibras.core.ui.theme.Red100
import kotlinx.coroutines.delay
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CameraActivity : ComponentActivity() {

    private val viewModel: CameraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val signName = intent.getStringExtra("SIGN_NAME_EXTRA") ?: "Praticar"
        val signId = intent.getIntExtra("SIGN_ID_EXTRA", -1)
        val currentSignIndex = intent.getIntExtra("CURRENT_SIGN_INDEX", 0)
        val totalSigns = intent.getIntExtra("TOTAL_SIGNS", 1)
        val moduleId = intent.getIntExtra("MODULE_ID_EXTRA", -1)

        setContent {
            KLibrasTheme(dynamicColor = false) {
                CameraScreen(
                    signName = signName,
                    signId = signId,
                    viewModel = viewModel,
                    currentSignIndex = currentSignIndex,
                    totalSigns = totalSigns,
                    moduleId = moduleId
                )
            }
        }
    }
}

enum class ScreenState {
    Camera,
    Processing,
    ResultA,
    ResultB
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraLayout(
    signName: String,
    onRecordClick: () -> Unit,
    onBackClick: () -> Unit,
    isRecording: Boolean,
    cameraController: LifecycleCameraController,
    lifecycleOwner: LifecycleOwner,
    isCameraReady: Boolean,
    onCameraReadyChanged: (Boolean) -> Unit,
    recordingTimer: Int
) {
    val context = LocalContext.current

    val permissions = arrayOf(
        Manifest.permission.CAMERA
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

    LaunchedEffect(hasPermissions, cameraController) {
        if (hasPermissions) {
            try {
                cameraController.setEnabledUseCases(
                    LifecycleCameraController.VIDEO_CAPTURE or LifecycleCameraController.IMAGE_CAPTURE
                )
                cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                cameraController.videoCaptureQualitySelector = QualitySelector.from(Quality.SD)
                cameraController.bindToLifecycle(lifecycleOwner)
                onCameraReadyChanged(true)
                Log.d("CameraX", "Camera controller bound successfully with VIDEO_CAPTURE enabled")
            } catch (t: Throwable) {
                Log.e("CameraX", "Failed to bind camera controller: ${t.message}", t)
                onCameraReadyChanged(false)
            }
        } else {
            try {
                cameraController.unbind()
            } catch (_: Exception) {}
            onCameraReadyChanged(false)
        }
    }

    Scaffold(
        containerColor = Color.Black,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = signName, fontWeight = FontWeight.Bold, color = Color.White)
                        if (isRecording) {
                            Box(
                                modifier = Modifier
                                    .background(Red100, shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = recordingTimer.toString(),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 24.sp
                                )
                            }
                        }
                    }
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
                    CameraPreview(lifecycleOwner = lifecycleOwner, cameraController = cameraController)
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
                            interactionSource = remember { MutableInteractionSource() },
                            enabled = isCameraReady && !isRecording
                        )
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 75.dp, height = 55.dp)
                            .background(
                                color = if (isRecording) Red100 else HighlightYellow,
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.camera_logo),
                            contentDescription = if (isRecording) "Gravando" else "Gravar",
                            modifier = Modifier.size(35.dp),
                            tint = if (isRecording) Color.White else Grey70
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isRecording) "Gravando..." else "Gravar",
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
            }
        }
    )
}

@Composable
fun CameraPreview(lifecycleOwner: LifecycleOwner, cameraController: LifecycleCameraController) {
    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                this.controller = cameraController
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun CameraScreen(
    signName: String,
    signId: Int,
    viewModel: CameraViewModel,
    currentSignIndex: Int,
    totalSigns: Int,
    moduleId: Int
) {
    var currentScreen by remember { mutableStateOf(ScreenState.Camera) }
    var isRecording by remember { mutableStateOf(false) }
    var currentRecording by remember { mutableStateOf<Recording?>(null) }
    var recordingTimer by remember { mutableStateOf(3) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainHandler = Handler(Looper.getMainLooper())

    val cameraController = remember { LifecycleCameraController(context) }

    var isCameraReady by remember { mutableStateOf(false) }

    val isLastSign = currentSignIndex >= totalSigns - 1

    LaunchedEffect(isRecording) {
        if (isRecording) {
            for (i in 3 downTo 0) {
                recordingTimer = i
                delay(1000)
            }
            Log.d("CameraX", "Timer ended, stopping recording")
            currentRecording?.stop()
        }
    }

    when (currentScreen) {
        ScreenState.Camera -> {
            CameraLayout(
                signName = signName,
                isRecording = isRecording,
                cameraController = cameraController,
                lifecycleOwner = lifecycleOwner,
                isCameraReady = isCameraReady,
                onCameraReadyChanged = { ready -> isCameraReady = ready },
                recordingTimer = recordingTimer,
                onRecordClick = {
                    if (!isCameraReady) {
                        mainHandler.post {
                            Toast.makeText(context, "Câmera não pronta para gravar", Toast.LENGTH_SHORT).show()
                        }
                        return@CameraLayout
                    }

                    if (!isRecording) {
                        val name = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                            .format(System.currentTimeMillis())

                        val videoFile = File(
                            context.getExternalFilesDir(null),
                            "KLibras_$name.mp4"
                        )

                        val outputOptions = FileOutputOptions.Builder(videoFile).build()
                        val audioConfig = AudioConfig.AUDIO_DISABLED

                        try {
                            currentRecording = cameraController.startRecording(
                                outputOptions,
                                audioConfig,
                                ContextCompat.getMainExecutor(context),
                                object : Consumer<VideoRecordEvent> {
                                    override fun accept(event: VideoRecordEvent) {
                                        when (event) {
                                            is VideoRecordEvent.Start -> {
                                                isRecording = true
                                                Log.d("CameraX", "Recording started")
                                            }
                                            is VideoRecordEvent.Finalize -> {
                                                isRecording = false
                                                if (!event.hasError()) {
                                                    Log.d("CameraX", "Recording saved: ${videoFile.absolutePath}")
                                                    currentScreen = ScreenState.Processing

                                                    viewModel.processVideo(
                                                        videoFile = videoFile,
                                                        signName = signName,
                                                        signId = signId,
                                                        context = context
                                                    ) { result ->
                                                        mainHandler.post {
                                                            when (result) {
                                                                true -> currentScreen = ScreenState.ResultA
                                                                false -> currentScreen = ScreenState.ResultB
                                                                null -> {
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Erro ao processar vídeo",
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                    currentScreen = ScreenState.Camera
                                                                }
                                                            }
                                                        }
                                                    }
                                                } else {
                                                    Log.e("CameraX", "Recording error: ${event.cause?.message}")
                                                    videoFile.delete()
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        } catch (ise: IllegalStateException) {
                            Log.e("CameraX", "Could not start recording - videoCapture disabled: ${ise.message}")
                            isCameraReady = false
                            mainHandler.post {
                                Toast.makeText(context, "Não foi possível iniciar a gravação", Toast.LENGTH_LONG).show()
                            }
                        } catch (t: Throwable) {
                            Log.e("CameraX", "Unexpected error starting recording: ${t.message}", t)
                            mainHandler.post {
                                Toast.makeText(context, "Erro ao iniciar gravação", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                onBackClick = {
                    currentRecording?.stop()
                    (context as? Activity)?.finish()
                }
            )
        }
        ScreenState.Processing -> {
            ProcessingScreen()
        }
        ScreenState.ResultA -> {
            ResultRight(
                onBackClick = {
                    viewModel.resetState()
                    currentScreen = ScreenState.Camera
                },
                onNavigateToNext = {
                    (context as? Activity)?.apply {
                        if (isLastSign && moduleId != -1) {
                            viewModel.addModuleToUser(context, moduleId) { success ->
                                mainHandler.post {
                                    if (success) {
                                        Toast.makeText(context, "Módulo concluído!", Toast.LENGTH_SHORT).show()
                                    }
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            }
                        } else {
                            setResult(Activity.RESULT_OK)
                            finish()
                        }
                    }
                }
            )
        }
        ScreenState.ResultB -> {
            ResultWrong(
                onBackClick = {
                    viewModel.resetState()
                    currentScreen = ScreenState.Camera
                },
                onNavigateToNext = {
                    (context as? Activity)?.apply {
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            )
        }
    }
}