package com.example.eventify.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.eventify.ui.components.AuthTextField
import com.example.eventify.ui.components.PrimaryButton
import com.example.eventify.ui.theme.EventifyTheme

@Composable
fun SignUpScreen(
    onSignUpClick: () -> Unit,
    onSignInClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Sign up to get started!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Inputs
        AuthTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full Name",
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardType = KeyboardType.Email
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            isPassword = true
        )
        Spacer(modifier = Modifier.height(16.dp))
        AuthTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password",
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            isPassword = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botão de Registo
        PrimaryButton(text = "Sign Up", onClick = onSignUpClick)

        Spacer(modifier = Modifier.height(16.dp))

        // Google Sign Up (Simulado)
        OutlinedButton(
            onClick = { /* TODO: Google Auth */ },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Sign up with Google", color = MaterialTheme.colorScheme.onBackground)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Link para voltar ao Login
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Already have an account? ", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSignInClick() }
            )
        }
    }
}

@Preview
@Composable
fun SignUpScreenPreview() {
    EventifyTheme(darkTheme = true) {
        SignUpScreen(onSignUpClick = {}, onSignInClick = {})
    }
}