package com.example.eventify.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Props: text, enabled, onClick.
// O botão adere ao M3, usando a cor primary e o shape small (8dp).
@Composable
fun PrimaryButton(
    text: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.small, // Usa o shape small (8dp) do seu EventifyTheme
        modifier = modifier
            .fillMaxWidth() // Ocupa toda a largura
            .height(56.dp)  // Altura de toque recomendada (M3 touch target)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelMedium // Usa a tipografia M3
        )
    }
}

// Preview para visualização no Android Studio
@Composable
fun PrimaryButtonPreview() {
    // Usar o seu EventifyTheme para visualizar as cores Nocturnal
    com.example.eventify.ui.theme.EventifyTheme {
        PrimaryButton(
            text = "Criar Evento",
            onClick = {},
            modifier = Modifier.fillMaxWidth()
        )
    }
}