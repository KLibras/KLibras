package com.br.klibras.features.camera

import CameraScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.br.klibras.core.ui.theme.KLibrasTheme

class CameraComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KLibrasTheme(dynamicColor = false) {
                CameraScreen()
            }
        }
    }
}