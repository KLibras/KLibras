package com.br.klibras.features.login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.br.klibras.R
import com.br.klibras.core.ui.theme.JosefinSans
import com.br.klibras.core.ui.theme.KLibrasTheme
import com.br.klibras.features.main.MainActivity
import com.br.klibras.features.register.RegisterComposeActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            KLibrasTheme(dynamicColor = false) {
                LoginScreen()
            }
        }
    }
}

/**
 * A tela de Login do aplicativo.
 * @param loginViewModel O ViewModel que gerencia o estado e a lógica de login.
 */
@Composable
fun LoginScreen(loginViewModel: LoginViewModel = viewModel()) {
    // Contexto atual, usado para navegação e Toasts.
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    // Estados para armazenar o email e a senha digitados pelo usuário.
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Estados para controlar a exibição de erros nos campos de texto.
    var isUsernameError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    // Raio dos cantos para os botões e campos de texto.
    val cornerRadius = 15.dp
    // Coleta o estado da UI do ViewModel para reagir a mudanças (Loading, Success, Error).
    val loginState by loginViewModel.loginUiState.collectAsState()

    // O serverClientId deve estar no seu arquivo strings.xml.
    val serverClientId = stringResource(id = R.string.server_client_id)
    val credentialManager = remember(context) { CredentialManager.create(context) }

    // Efeito que executa uma ação quando o loginState muda.
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginUiState.Success -> { // Em caso de sucesso
               /* Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()*/
                // Navega para a MainActivity e limpa a pilha de navegação.
                navigateToMain(context)
            }
            is LoginUiState.Error -> { // Em caso de erro
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> { /* Não faz nada para outros estados como Idle ou Loading */ }
        }
    }

    // Box serve como um contêiner que permite sobrepor elementos (como o loading).
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
                    color = MaterialTheme.colorScheme.onBackground, // Cor do texto do tema.
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = JosefinSans
                )
            }

            Spacer(modifier = Modifier.height(64.dp)) // Espaço antes dos campos de texto.

            // Campo de texto para o email.
            OutlinedTextField(
                value = username,
                onValueChange = {
                    if (it.length <= 50) username = it
                    isUsernameError = false // Reseta o erro ao digitar.
                },
                label = { Text("Insira seu usuário") },
                leadingIcon = { Icon(painterResource(id = R.drawable.mail_svg), contentDescription = "Email icon") },
                modifier = Modifier.fillMaxWidth(), // Ocupa toda a largura.
                shape = RoundedCornerShape(cornerRadius), // Bordas arredondadas.
                isError = isUsernameError, // Mostra o estado de erro.
                supportingText = { if (isUsernameError) Text("Este campo não pode estar em branco", color = Color.Red) },
                singleLine = true // Força o campo a ter uma única linha.
            )

            Spacer(modifier = Modifier.height(16.dp)) // Espaço entre os campos.

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

            Spacer(modifier = Modifier.height(8.dp))

            // Texto "Esqueceu a senha?".
            Text(
                text = "Esqueceu a senha?",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End) // Alinha à direita.
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Botão de Login.
            Button(
                onClick = {
                    // Validação básica para não enviar campos vazios.
                    isUsernameError = username.isBlank()
                    isPasswordError = password.isBlank()
                    if (!isUsernameError && !isPasswordError) {
                        // Ação do botão agora chama o ViewModel
                        loginViewModel.login(username, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEBC32)), // Cor de fundo amarela.
                shape = RoundedCornerShape(cornerRadius), // Bordas arredondadas.
                enabled = loginState !is LoginUiState.Loading // Desabilita o botão durante o carregamento.
            ) {
                Text("Login", color = Color.Black, fontFamily = JosefinSans, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Divisor "ou".
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFCCCCCC))
                Text(text = "ou", color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFCCCCCC))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {

                    val getGoogleIdOption = GetGoogleIdOption.Builder()
                        .setServerClientId(serverClientId)
                        .setFilterByAuthorizedAccounts(false)
                        .build()

                    val credentialRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(getGoogleIdOption)
                        .build()


                    coroutineScope.launch {
                        try {
                            val result = credentialManager.getCredential(context, credentialRequest)
                            val credential = result.credential

                            if (credential is GoogleIdTokenCredential) {
                                val idToken = credential.idToken
                                Log.d("GoogleSignIn", "ID Token recebido: $idToken")

                                // TODO: Envie o idToken para o seu backend para verificação e autenticação.
                                Toast.makeText(context, "Login com Google bem-sucedido!", Toast.LENGTH_SHORT).show()
                                navigateToMain(context)

                            } else {
                                Log.e("GoogleSignIn", "A credencial não é do tipo GoogleIdTokenCredential")
                                Toast.makeText(context, "Erro no login com Google.", Toast.LENGTH_LONG).show()
                            }
                        } catch (e: GetCredentialException) {
                            Log.e("GoogleSignIn", "Erro ao obter credencial: ${e.message}", e)
                            Toast.makeText(context, "Falha no login com Google.", Toast.LENGTH_LONG).show()
                        }
                    }
                },
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

            // Texto e link para a tela de registro.
            Row {
                ClickableText(
                    text = AnnotatedString("Não tem uma conta?  Registre-se"),
                    style = TextStyle(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    onClick = {
                        // Navega para a tela de registro.
                        val intent = Intent(context, RegisterComposeActivity::class.java)
                        startActivity(context, intent, null)
                    }
                )
            }
        }

        // Overlay de carregamento.
        if (loginState is LoginUiState.Loading) {
            Box(
                contentAlignment = Alignment.Center, // Centraliza o indicador de progresso.
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)) // Fundo semitransparente.
            ) {
                CircularProgressIndicator(color = Color(0xFFDEBC32)) // Indicador de progresso amarelo.
            }
        }
    }
}

/**
 * Função auxiliar para centralizar a navegação para a tela principal.
 */
private fun navigateToMain(context: Context) {
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    startActivity(context, intent, null)
}

