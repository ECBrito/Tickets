package com.example.eventify.ui.screens.organizer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // <--- O IMPORT QUE CORRIGE O ERRO DO 'INT'
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.Event
import com.example.eventify.ui.theme.EventifyTheme

@Composable
fun OrganizerDashboardScreen(
    onCreateEventClick: () -> Unit,
    onEventClick: (String) -> Unit
) {
    // Agora esta função já existe no AppModule
    val viewModel = remember { AppModule.provideOrganizerViewModel() }

    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0B0A12),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEventClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Event")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { OrganizerHeader() }

            // Lista de eventos
            if (events.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        if (isLoading) {
                            CircularProgressIndicator()
                        } else {
                            Text("No events created yet.", color = Color.Gray)
                        }
                    }
                }
            } else {
                // O import lá em cima (lazy.items) faz isto funcionar
                items(events) { event ->
                    RecentEventItem(event) { onEventClick(event.id) }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
fun OrganizerHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Dashboard", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Welcome back!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Surface(shape = CircleShape, modifier = Modifier.size(40.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=500",
                contentDescription = "Profile",
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun RecentEventItem(event: Event, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151520)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                AsyncImage(model = event.imageUrl, contentDescription = null, contentScale = ContentScale.Crop)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                Text(event.location, style = MaterialTheme.typography.bodyMedium, color = Color.Gray, maxLines = 1)
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray)
            }
        }
    }
}

@Preview
@Composable
fun OrganizerDashboardPreview() {
    EventifyTheme(darkTheme = true) {
        // Preview vazio ou com mocks
    }
}