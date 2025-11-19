package com.example.eventify.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

// Definições de Cores (usando os tokens hexadecimais)
val PurpleRich = Color(0xFF6B49FF)
val White = Color(0xFFFFFFFF)
val PurpleContainerDark = Color(0xFF3D1D8A)
val LavenderLight = Color(0xFFEDE8FF)
val PurpleAccent = Color(0xFF9A8CFF)
val NearBlack = Color(0xFF0B0A12)
val DarkSurface = Color(0xFF0F1020)
val DarkSurfaceVariant = Color(0xFF1A1426)
val ErrorRed = Color(0xFFFF6B6B)
val OutlineDark = Color(0xFF2A2433)

// Cores do Tema Nocturnal (Dark Theme) - Usa DarkColorScheme
val DarkColorScheme = darkColorScheme(
    primary = PurpleRich,           // #6B49FF
    onPrimary = White,              // #FFFFFF
    primaryContainer = PurpleContainerDark, // #3D1D8A
    onPrimaryContainer = LavenderLight,     // #EDE8FF
    secondary = PurpleAccent,       // #9A8CFF
    background = NearBlack,         // #0B0A12
    onBackground = White,
    surface = DarkSurface,          // #0F1020
    onSurface = LavenderLight,
    surfaceVariant = DarkSurfaceVariant, // #1A1426
    error = ErrorRed,               // #FF6B6B
    outline = OutlineDark           // #2A2433
)

// Cores do Tema Neutral (Light Theme) - Usa LightColorScheme
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4B39E6),    // #4B39E6
    onPrimary = White,
    background = White,             // #FFFFFF
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFF6F5FA),    // #F6F5FA
    onSurface = Color(0xFF1C1B1F),
    surfaceVariant = Color(0xFFECE8F8), // #ECE8F8
    outline = Color(0xFFE0DBEF)      // #E0DBEF
)