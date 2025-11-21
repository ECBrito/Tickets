package com.example.eventify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.eventify.model.Event
import com.example.eventify.ui.components.EventCard
import com.example.eventify.viewmodels.ExploreViewModelKMM

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModelKMM,
    onEventClick: (String) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState(initial = "")
    val filteredEvents by viewModel.filteredEvents.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState(initial = false)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.onSearchQueryChange(it) },
            label = { Text("Search events") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredEvents) { event: Event ->
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event.id) },
                        onSave = { /* futura lógica de save */ }
                    )
                }
            }
        }
    }
}
