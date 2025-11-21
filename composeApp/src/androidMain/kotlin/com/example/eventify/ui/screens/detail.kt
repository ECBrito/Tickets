package com.example.eventify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepositoryImplKMM
import com.example.eventify.viewmodels.EventDetailViewModelKMM

@Composable
fun EventDetailScreen(eventId: String, userId: String, navController: NavController) {
    val repository = remember { EventRepositoryImplKMM() }
    val viewModel = remember { EventDetailViewModelKMM(repository, eventId, userId) }

    val event by viewModel.event.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) {
        Text("Loading...")
    } else {
        event?.let { EventDetailContent(it) { viewModel.toggleRsvp() } }
    }
}

@Composable
fun EventDetailContent(event: Event, onRsvpClick: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(event.title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(event.description)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRsvpClick) {
            Text(if (event.isRegistered) "Unregister" else "Register")
        }
    }
}
