
package com.br.klibras.features.login

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.br.klibras.R
import com.br.klibras.core.ui.theme.KLibrasTheme

/**
 * Activity que hospeda a tela de Registro.
 */
class RegisterComposeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KLibrasTheme(dynamicColor = false) {
                RegisterScreen()
            }
        }
    }
}

/**
 * A tela de Registro do aplicativo.
 */
@Composable
fun RegisterScreen() {
    // Contexto atual, usado para navegação e Toasts.
    val context = LocalContext.current
    // Estados para armazenar os dados de registro digitados pelo usuário.
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Estados para controlar a exibição de erros nos campos de texto.
    var isEmailError by remember { mutableStateOf(false) }
    var isUsernameError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    // Raio dos cantos para os botões e campos de texto.
    val cornerRadius = 15.dp

    // Box serve como um contêiner raiz.
    Box(modifier = Modifier.fillMaxSize()) {
        // Column principal que organiza todo o conteúdo da tela verticalmente.
        Column(
            modifier = Modifier
                .fillMaxSize() // Ocupa todo o espaço disponível.
                .background(MaterialTheme.colorScheme.background) // Cor de fundo do tema.
                .padding(16.dp), // Espaçamento interno.
            horizontalAlignment = Alignment.CenterHorizontally // Centraliza todo o conteúdo horizontalmente.
        ) {
            // Espaçador para criar uma margem no topo.
            Spacer(modifier = Modifier.height(104.dp))

            // Row para o logo e o nome do app.
            Row(verticalAlignment = Alignment.CenterVertically) { // Alinha itens verticalmente no centro.
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Libras Logo",
                    modifier = Modifier.size(63.dp, 56.dp)
                )
                Spacer(modifier = Modifier.width(8.dp)) // Espaço entre o logo e o texto.
                Text(
                    text = "KLibras",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
                )
            }

            Spacer(modifier = Modifier.height(64.dp)) // Espaço antes dos campos de texto.

            // Campo de texto para o email.
            OutlinedTextField(
                value = email,
                onValueChange = {
                    if (it.length <= 50) email = it
                    isEmailError = false // Reseta o erro ao digitar.
                },
                label = { Text("Insira seu email") },
                leadingIcon = { Icon(painterResource(id = R.drawable.mail_svg), contentDescription = "Email icon") },
                modifier = Modifier.fillMaxWidth(), // Ocupa toda a largura.
                shape = RoundedCornerShape(cornerRadius), // Bordas arredondadas.
                isError = isEmailError, // Mostra o estado de erro.
                supportingText = { if (isEmailError) Text("Este campo não pode estar em branco", color = Color.Red) },
                singleLine = true // Força o campo a ter uma única linha.
            )
            Spacer(modifier = Modifier.height(16.dp)) // Espaço entre os campos.

            // Campo de texto para o nome de usuário.
            OutlinedTextField(
                value = username,
                onValueChange = {
                    if (it.length <= 20) username = it
                    isUsernameError = false
                },
                label = { Text("Insira seu nome de usuário") },
                leadingIcon = { Icon(painterResource(id = R.drawable.account_svg), contentDescription = "User icon") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cornerRadius),
                isError = isUsernameError,
                supportingText = { if (isUsernameError) Text("Este campo não pode estar em branco", color = Color.Red) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Campo de texto para a senha.
            OutlinedTextField(
                value = password,
                onValueChange = {
                    if (it.length <= 15) password = it
                    isPasswordError = false
                },
                label = { Text("Insira sua senha") },
                leadingIcon = { Icon(painterResource(id = R.drawable.lock_svg), contentDescription = "Password icon") },
                visualTransformation = PasswordVisualTransformation(), // Esconde a senha.
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cornerRadius),
                isError = isPasswordError,
                supportingText = { if (isPasswordError) Text("Este campo não pode estar em branco", color = Color.Red) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botão de Registro.
            Button(
                onClick = {
                    // Valida os campos antes de prosseguir.
                    isEmailError = email.isBlank()
                    isUsernameError = username.isBlank()
                    isPasswordError = password.isBlank()
                    if (!isEmailError && !isPasswordError && !isUsernameError) {
                        // TODO: Implementar a lógica de registro real.
                        Toast.makeText(context, "Registro Clicado!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEBC32)), // Cor de fundo amarela.
                shape = RoundedCornerShape(cornerRadius) // Bordas arredondadas.
            ) {
                Text("Registre-se", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Divisor "ou".
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f), // Ocupa o espaço disponível.
                    color = Color(0xFFCCCCCC) // Cor cinza clara.
                )
                Text(text = "ou", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFCCCCCC)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão de login com Google.
            Button(
                onClick = { /* TODO: Implement Google Sign In */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White), // Fundo branco.
                shape = RoundedCornerShape(cornerRadius)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Google", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Texto e link para voltar para a tela de login.
            Row {
                ClickableText(
                    text = AnnotatedString("Tem uma conta?  Faça login"),
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    onClick = {
                        // Fecha a tela de registro para voltar à de login.
                        val activity = (context as? Activity)
                        activity?.finish()
                    }
                )
            }
        }
    }
}
