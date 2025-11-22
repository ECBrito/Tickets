package com.example.eventify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventify.di.AppModule // <--- IMPORTANTE
import com.example.eventify.model.Event
// O ViewModel é importado automaticamente se o package estiver certo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    userId: String, // Mantemos o param caso a navegação o envie, mas o AppModule usa um mock interno por agora
    navController: NavController
) {
    // CORREÇÃO: Usar o AppModule para obter o ViewModel configurado
    val viewModel = remember { AppModule.provideEventDetailViewModel(eventId) }

    val event by viewModel.event.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                // O 'let' garante que só mostramos o conteúdo se o evento já tiver carregado
                event?.let { currentEvent ->
                    EventDetailContent(
                        event = currentEvent,
                        // Lê o estado 'isRegistered' do ViewModel ou do Evento
                        isRegistered = viewModel.isRegistered,
                        onRsvpClick = { viewModel.toggleRsvp() }
                    )
                }
            }
        }
    }
}

@Composable
fun EventDetailContent(
    event: Event,
    isRegistered: Boolean,
    onRsvpClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(event.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))

        Text(
            text = "Location: ${event.location}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(Modifier.height(16.dp))

        Text(event.description)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onRsvpClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isRegistered) "Unregister" else "Register")
        }
    }
}