package com.example.eventify.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventify.auth.AuthState
import com.example.eventify.ui.components.AuthTextField
import com.example.eventify.ui.components.PrimaryButton
import com.example.eventify.ui.theme.EventifyTheme
import com.example.eventify.ui.viewmodels.AuthViewModel
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SignInScreen(
    onSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val authState by viewModel.authState.collectAsState()
    val isAuthenticating = authState == AuthState.Initial

    LaunchedEffect(authState) {
        if (authState == AuthState.Authenticated) {
            onSignInClick()
        }
    }

    // Usamos um Box para permitir elementos sobrepostos ou fundos complexos
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Ícone num contentor circular para dar profundidade
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Eventify",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Inputs organizados
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = { Icon(Icons.Default.Email, null) },
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(16.dp))

            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                leadingIcon = { Icon(Icons.Default.Lock, null) },
                isPassword = true
            )

            // Forgot Password alinhado à direita com estilo subtil
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onForgotPasswordClick) {
                    Text(
                        "Forgot password?",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Botão Principal - Mais alto e com cantos arredondados (M3)
            PrimaryButton(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                text = "Sign In",
                enabled = !isAuthenticating && email.isNotBlank() && password.isNotBlank(),
                onClick = { viewModel.signIn(email, password) }
            )

            // Erro com visual de "Toast" ou Card integrado
            if (authState is AuthState.Error) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Footer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Don't have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign up",
                    modifier = Modifier.clickable { onSignUpClick() },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignInScreenPreview() {
    EventifyTheme(darkTheme = false) {
        SignInScreen(onSignInClick = {}, onForgotPasswordClick = {}, onSignUpClick = {})
    }
}