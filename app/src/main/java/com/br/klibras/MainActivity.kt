package com.br.klibras

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity(), HandLandmarkerHelper.LandmarkerListener {
    private lateinit var handLandmarkerHelper: HandLandmarkerHelper
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var imageAnalysis: ImageAnalysis
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var overlayView: OverlayView // Classe que desenha as mãos

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicia o helper
        handLandmarkerHelper = HandLandmarkerHelper(
            context = this,
            runningMode = RunningMode.LIVE_STREAM, // Modo "ao vivo"
            maxNumHands = 2, // Detecta no máximo 2 mãos
            handLandmarkerHelperListener = this
        )

        // Inicia o executor em uma thread unica pra analisar as images em uma thread diferente ta main
        cameraExecutor = Executors.newSingleThreadExecutor()


        // Setup do compose
        setContent {
            HandLandmarkerApp(handLandmarkerHelper, cameraExecutor)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::handLandmarkerHelper.isInitialized) { // Pra garantir que o helper ta iniciado
            handLandmarkerHelper = HandLandmarkerHelper(
                context = this,
                runningMode = RunningMode.LIVE_STREAM,
                maxNumHands = 2, // Support 2 hands
                handLandmarkerHelperListener = this
            )
        }
    }

    override fun onPause() {
        super.onPause()
        handLandmarkerHelper.clearHandLandmarker() // Limpa recursos quando tiver pausado
    }


    // Quando para limpa os recursos e destroi a thread
    override fun onDestroy() {
        super.onDestroy()
        handLandmarkerHelper.clearHandLandmarker()
        cameraExecutor.shutdown()
    }


    // É chamado pra mostrar os desenhos das mãos
    /* TODO! Aqui tem que mudar pra adicionar a função de gravar só quando a mão for detectada,
        ai pode apagar a OverlayView e apagar o que ta em baixo e adiciona a função de gravar
        tipo totalHand > 0 ai o frame vai ta no processImage()
    */
    override fun onResults(resultBundle: HandLandmarkerHelper.ResultBundle) {
        runOnUiThread {
            // Atualiza os desenhos das mãos
            overlayView.setResults(
                handLandmarkerResults = resultBundle.results,
                imageHeight = resultBundle.inputImageHeight,
                imageWidth = resultBundle.inputImageWidth,
                runningMode = RunningMode.LIVE_STREAM,
                isFrontCamera = true // Fala que é a camera da frente que ta lendo, se tiver em 'false' o desenho da mão vai ser ao contrário
            )
        }
    }

    override fun onError(error: String, errorCode: Int) {
        runOnUiThread {
            Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            // Tenta fazer a gpu assumir o processo, se falhar o processador pega o processo
            if (errorCode == HandLandmarkerHelper.GPU_ERROR) {
                handLandmarkerHelper = HandLandmarkerHelper(
                    context = this,
                    currentDelegate = HandLandmarkerHelper.DELEGATE_CPU,
                    runningMode = RunningMode.LIVE_STREAM,
                    maxNumHands = 2,
                    handLandmarkerHelperListener = this
                )
            }
        }
    }


    // Processa cada frame
    private fun processImage(imageProxy: ImageProxy) {
        // Converte o imageProxy pra bitmap pro mediapipe ler
        val bitmap = imageProxy.toBitmap()
        // Pra detectar as mão
        handLandmarkerHelper.detectLiveStream(bitmap, isFrontCamera = true)
        // Fecha o proxy pra liberar recursos
        imageProxy.close()
    }

    // Starta a camera e começa a analise
    fun startCamera(previewView: PreviewView, overlay: OverlayView) {
        overlayView = overlay

        // Checa a permissão da camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Uso da câmera não está autorizado", Toast.LENGTH_SHORT).show()
            return
        }

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { it.setSurfaceProvider(previewView.surfaceProvider) }


            imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(480, 360)) // Define a resolução, ta baixo por causa de performance
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        processImage(imageProxy)
                    }
                }

            // Força só a camera da frente
            // TODO! Adicionar a opção de escolher o lado da camera
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()


                if (cameraProvider.hasCamera(cameraSelector)) {
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                } else {
                    Toast.makeText(this, "Cãmera da frente não disponível", Toast.LENGTH_SHORT).show()
                }
            } catch (exc: Exception) {
                Toast.makeText(this, "Escolha da câmera falhou: ${exc.message}", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }
}

@Composable
fun HandLandmarkerApp(handLandmarkerHelper: HandLandmarkerHelper, cameraExecutor: ExecutorService) {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            CameraPreview(handLandmarkerHelper, cameraExecutor)
        }
    }
}

@Composable
fun CameraPreview(handLandmarkerHelper: HandLandmarkerHelper, cameraExecutor: ExecutorService) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(factory = { ctx ->
        val frameLayout = FrameLayout(ctx)
        val previewView = PreviewView(ctx)
        val overlayView = OverlayView(ctx, null)

        frameLayout.addView(previewView)
        frameLayout.addView(overlayView)

        val mainActivity = context as MainActivity
        mainActivity.startCamera(previewView, overlayView)

        frameLayout
    }, modifier = Modifier.fillMaxSize())
}