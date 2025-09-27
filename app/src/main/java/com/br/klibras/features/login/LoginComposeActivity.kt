package com.br.klibras.features.login

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.br.klibras.core.ui.theme.KLibrasTheme


class LoginComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KLibrasTheme(dynamicColor = false) {
                LoginScreen()
            }
        }
    }
}