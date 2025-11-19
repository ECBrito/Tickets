package com.example.eventify.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.eventify.ui.components.AuthTextField
import com.example.eventify.ui.components.PrimaryButton
import com.example.eventify.ui.theme.EventifyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onSendLinkClick: (email: String) -> Unit
) {
    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { /* Título vazio */ },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Forgot Password?",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Enter the email address associated with your account and we'll send a link to reset your password.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(48.dp))

            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email Address",
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "Email Icon"
                    )
                },
                keyboardType = KeyboardType.Email
            )

            Spacer(modifier = Modifier.height(32.dp))

            PrimaryButton(
                text = "Send Reset Link",
                onClick = { onSendLinkClick(email) }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun ForgotPasswordScreenPreview() {
    EventifyTheme(darkTheme = true) {
        ForgotPasswordScreen(
            onBackClick = {},
            onSendLinkClick = {}
        )
    }
}