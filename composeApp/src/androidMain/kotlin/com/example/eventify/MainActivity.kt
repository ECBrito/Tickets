package com.example.eventify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.eventify.ui.theme.EventifyTheme
// Importar a fábrica de drivers (que está no módulo shared)
import com.example.eventify.db.DatabaseDriverFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Criar a fábrica de drivers (no Android, precisa do contexto)
        val driverFactory = DatabaseDriverFactory(context = applicationContext)

        // 2. Inicializar o repositório, passando a fábrica


        // Opcional: Se usares o Storage, o applicationContext precisa de ser inicializado aqui
        // storage.applicationContext = applicationContext

        enableEdgeToEdge()
        setContent {
            EventifyTheme(darkTheme = true) {
                // Aqui vais querer usar o teu AppMain() que liga os ViewModels.
                // Mas como estamos a limpar, vou usar um placeholder simples.
                AppMain()
            }
        }
    }
}