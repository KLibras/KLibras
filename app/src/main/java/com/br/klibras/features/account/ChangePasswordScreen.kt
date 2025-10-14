package com.br.klibras.features.account

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.br.klibras.shared.CustomInput
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Alterar Senha") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) { // Botão para voltar
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center // <-- ESTA LINHA CENTRALIZA O CONTEÚDO
        ) {
            CustomInput(
                hint = "Digite a nova senha",
                onSendClick = { newPassword ->
                    coroutineScope.launch {
                        Toast.makeText(context, "Salvando nova senha...", Toast.LENGTH_SHORT).show()

                        // TODO: Substitua o 'delay' pela sua chamada de API real
                        delay(2000)

                        Toast.makeText(context, "Senha alterada com sucesso!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                }
            )
        }
    }
}