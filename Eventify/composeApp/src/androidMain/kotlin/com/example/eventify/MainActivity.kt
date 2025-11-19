package com.example.eventify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.eventify.ui.EventifyNavHost
import com.example.eventify.ui.theme.EventifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Para a app ocupar o ecrã todo (atrás da barra de status)
        setContent {
            // Define o tema (podes forçar darkTheme = true se quiseres testar o modo Nocturnal)
            EventifyTheme(darkTheme = true) {
                // Cria o controlador de navegação principal
                val navController = rememberNavController()

                // Inicia a árvore de navegação
                EventifyNavHost(navController = navController)
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    EventifyTheme(darkTheme = true) {
        EventifyNavHost(navController = rememberNavController())
    }
}