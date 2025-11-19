package com.example.eventify.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

// Componente para campos de input de autenticação (Email, Senha, etc.)
@Composable
fun AuthTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    val showPassword = remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon ?: if (isPassword) { { Icon(Icons.Default.Lock, contentDescription = null) } } else null,
        visualTransformation = when {
            isPassword && !showPassword.value -> PasswordVisualTransformation()
            else -> VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        isError = isError,
        supportingText = {
            if (isError && errorMessage != null) {
                Text(text = errorMessage)
            }
        },
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { showPassword.value = !showPassword.value }) {
                    Icon(
                        imageVector = if (showPassword.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle password visibility"
                    )
                }
            }
        } else null,
        modifier = modifier.fillMaxWidth()
    )
}

// Preview para visualização no Android Studio
@Composable
fun AuthTextFieldPreview() {
    com.example.eventify.ui.theme.EventifyTheme {
        AuthTextField(
            value = "teste@email.com",
            label = "Email",
            onValueChange = {},
            keyboardType = KeyboardType.Email,
            isPassword = false
        )
    }
}