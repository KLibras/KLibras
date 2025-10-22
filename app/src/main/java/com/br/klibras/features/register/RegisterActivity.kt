package com.br.klibras.features.register

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.br.klibras.R
import com.br.klibras.core.ui.theme.JosefinSans
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
fun RegisterScreen(registerViewModel: RegisterViewModel = viewModel()) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isEmailError by remember { mutableStateOf(false) }
    var isUsernameError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    val cornerRadius = 15.dp
    val registerState by registerViewModel.registerState.collectAsState()

    // Efeito para reagir a mudanças no estado de registro
    LaunchedEffect(registerState) {
        when (val state = registerState) {
            is RegisterUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                // Fecha a activity para voltar à tela de login
                (context as? Activity)?.finish()
            }
            is RegisterUiState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> { /* Não faz nada para Idle ou Loading */ }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(104.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Libras Logo",
                    modifier = Modifier.size(63.dp, 56.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "KLibras",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JosefinSans
                )
            }

            Spacer(modifier = Modifier.height(64.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    if (it.length <= 50) email = it
                    isEmailError = false
                },
                label = { Text("Insira seu email") },
                leadingIcon = { Icon(painterResource(id = R.drawable.mail_svg), contentDescription = "Email icon") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cornerRadius),
                isError = isEmailError,
                supportingText = { if (isEmailError) Text("Este campo não pode estar em branco", color = Color.Red) },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(16.dp))

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

            OutlinedTextField(
                value = password,
                onValueChange = {
                    if (it.length <= 15) password = it
                    isPasswordError = false
                },
                label = { Text("Insira sua senha") },
                leadingIcon = { Icon(painterResource(id = R.drawable.lock_svg), contentDescription = "Password icon") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cornerRadius),
                isError = isPasswordError,
                supportingText = { if (isPasswordError) Text("Este campo não pode estar em branco", color = Color.Red) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isEmailError = email.isBlank()
                    isUsernameError = username.isBlank()
                    isPasswordError = password.isBlank()
                    if (!isEmailError && !isPasswordError && !isUsernameError) {
                        registerViewModel.register(email, username, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEBC32)),
                shape = RoundedCornerShape(cornerRadius),
                enabled = registerState !is RegisterUiState.Loading // Desabilita durante o carregamento
            ) {
                Text("Registre-se", color = Color.Black, fontFamily = JosefinSans, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFCCCCCC))
                Text(text = "ou", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFCCCCCC))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { /* TODO: Implement Google Sign In */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
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

            Row {
                ClickableText(
                    text = AnnotatedString("Tem uma conta?  Faça login"),
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    onClick = {
                        (context as? Activity)?.finish()
                    }
                )
            }
        }

        // Overlay de carregamento
        if (registerState is RegisterUiState.Loading) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                CircularProgressIndicator(color = Color(0xFFDEBC32))
            }
        }
    }
}