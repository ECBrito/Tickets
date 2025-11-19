package com.example.eventify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.eventify.db.DatabaseDriverFactory // Importar a Factory
import com.example.eventify.repository.EventRepository // Importar o Repositório
import com.example.eventify.ui.EventifyNavHost
import com.example.eventify.ui.theme.EventifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializar a Base de Dados SQLDelight
        val driverFactory = DatabaseDriverFactory(context = applicationContext)
        EventRepository.initialize(driverFactory)

        enableEdgeToEdge()
        setContent {
            EventifyTheme(darkTheme = true) {
                val navController = rememberNavController()
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