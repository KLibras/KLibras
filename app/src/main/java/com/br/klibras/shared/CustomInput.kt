package com.br.klibras.shared

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.br.klibras.core.ui.theme.HighlightYellow

/**
 * Um Composable reutilizável que consiste em um campo de texto centrado e um botão de ação abaixo.
 *
 * @param hint O texto de placeholder para o campo de texto.
 * @param buttonText O texto a ser exibido no botão.
 * @param onSendClick Ação a ser executada quando o botão é clicado,
 * passando o texto atual do campo como argumento.
 */
@Composable
fun CustomInput(
    modifier: Modifier = Modifier,
    hint: String = "Digite algo...",
    buttonText: String = "Alterar",
    onSendClick: (String) -> Unit
) {
    // Estado para armazenar o texto digitado pelo usuário.
    var text by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Campo de texto customizado, seguindo o estilo do seu app.
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            label = { Text(hint) },
            modifier = Modifier.fillMaxWidth(), // Ocupa a largura total
            shape = RoundedCornerShape(15.dp),
            singleLine = true
        )

        // Espaçador para criar a margem de 25.dp
        Spacer(modifier = Modifier.height(25.dp))

        // Botão de ação.
        Button(
            onClick = {
                onSendClick(text) // Chama a função lambda passando o texto atual
            },
            shape = RoundedCornerShape(15.dp), // Bordas arredondadas para combinar
            colors = ButtonDefaults.buttonColors(containerColor = HighlightYellow),
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp) // Altura padrão do botão
        ) {
            Text(
                text = buttonText,
                color = Color.Black,
                fontWeight = FontWeight.Bold
            )
        }
    }
}