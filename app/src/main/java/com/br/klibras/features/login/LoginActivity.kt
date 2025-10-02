package com.br.klibras.features.login

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.br.klibras.R
import com.br.klibras.features.main.MainActivity

@Composable
fun LoginScreen(loginViewModel: LoginViewModel = viewModel()) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var isEmailError by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    val cornerRadius = 24.dp
    // Collect the state from the ViewModel
    val loginState by loginViewModel.loginUiState.collectAsState()

    // This block observes the login state. When it changes, it performs an action.
    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is LoginUiState.Success -> {
                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, MainActivity::class.java).apply {
                    // Clear the back stack so the user can't go back to the login screen
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(context, intent, null)
            }
            is LoginUiState.Error -> {
                // Show an error message and then reset the state
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                // Assuming you add a function in your ViewModel to reset the state
                // loginViewModel.dismissError()
            }
            else -> { /* Idle or Loading state, do nothing here */ }
        }
    }


    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5DC))
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
                    color = Color(0xFF333333),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif
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

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Esqueceu a senha?",
                color = Color(0xFF333333),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    // Validate fields and call the ViewModel
                    isEmailError = email.isBlank()
                    isPasswordError = password.isBlank()
                    if (!isEmailError && !isPasswordError) {
                        loginViewModel.login(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDEBC32)),
                shape = RoundedCornerShape(cornerRadius),
                // Disable the button while loading
                enabled = loginState !is LoginUiState.Loading
            ) {
                Text("Login", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(32.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = DividerDefaults.Thickness,
                    color = Color(0xFFCCCCCC)
                )
                Text(text = "ou", color = Color(0xFF333333), modifier = Modifier.padding(horizontal = 8.dp))
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    thickness = DividerDefaults.Thickness,
                    color = Color(0xFFCCCCCC)
                )
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
                Text(text = "Google", color = Color(0xFF333333), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }



            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Text(
                    text = "Não tem uma conta?",
                    color = Color(0xFF333333),
                    fontSize = 12.sp
                )
                Text(
                    text = "Registre-se",
                    color = Color(0xFF333333),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }

        // Show a loading indicator when the state is Loading
        if (loginState is LoginUiState.Loading) {
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

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}