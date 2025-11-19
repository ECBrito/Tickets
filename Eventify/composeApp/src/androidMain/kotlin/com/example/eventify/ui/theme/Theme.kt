package com.example.eventify.ui.theme


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

// Você precisará definir a Tipografia e Formas aqui
// Por enquanto, usaremos a Tipografia e Formas Padrão do M3.

// TODO: Definir EventifyTypography (Com Roboto/Google Sans)
val EventifyTypography = Typography() // Substituir pela sua escala M3 customizada

val EventifyShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(18.dp)
)


@Composable
fun EventifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Detecta o tema do sistema
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme // Tema Nocturnal
    } else {
        LightColorScheme // Tema Neutral
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = EventifyTypography, // Aplica a Tipografia
        shapes = EventifyShapes,         // Aplica as Formas
        content = content
    )
}