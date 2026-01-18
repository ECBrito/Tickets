package com.example.eventify.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventify.ui.components.AuthTextField
import com.example.eventify.ui.components.PrimaryButton
import com.example.eventify.ui.theme.EventifyTheme
import com.example.eventify.ui.viewmodels.RegisterViewModel

import com.example.eventify.R
@Composable
fun SignUpScreen(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    val registerViewModel: RegisterViewModel = viewModel()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Títulos com peso visual
            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Sign up to get started!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Grupo de Inputs
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AuthTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Full Name",
                    leadingIcon = { Icon(Icons.Default.Person, null) }
                )
                AuthTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Email",
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    keyboardType = KeyboardType.Email
                )
                AuthTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    isPassword = true
                )
                AuthTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirm Password",
                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                    isPassword = true
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botão Principal
            PrimaryButton(
                text = if (isLoading) "Creating..." else "Sign Up",
                enabled = !isLoading,
                onClick = {
                    if (password != confirmPassword) {
                        errorMessage = "Passwords do not match"
                        return@PrimaryButton
                    }
                    isLoading = true
                    errorMessage = null
                    registerViewModel.registerOrganizer(
                        name = name, email = email, password = password,
                        onSuccess = { isLoading = false; onSignUpClick() },
                        onError = { error -> isLoading = false; errorMessage = error }
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botão Google Moderno
            OutlinedButton(
                onClick = { /* TODO: Google Auth */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Substitui pelo teu painterResource do logo do google
                    Icon(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified // Mantém as cores originais do logo
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Sign up with Google", style = MaterialTheme.typography.titleMedium)
                }
            }

            // Mensagem de Erro formatada
            if (!errorMessage.isNullOrEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Footer ajustado
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Sign In",
                    modifier = Modifier.clickable { onSignInClick() },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}