package com.example.eventify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.eventify.storage.applicationContext // Importar a variável global
import com.example.eventify.ui.theme.EventifyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar o contexto para o File Upload (Storage)
        applicationContext = applicationContext

        enableEdgeToEdge()
        setContent {
            EventifyTheme(darkTheme = true) {
                // O AppMain gere a navegação baseada no Login
                AppMain()
            }
        }
    }
}