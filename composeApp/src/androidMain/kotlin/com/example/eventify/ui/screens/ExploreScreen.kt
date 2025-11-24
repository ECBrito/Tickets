package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder // Importante
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.Event
import com.example.eventify.model.FilterState
import com.example.eventify.ui.components.FilterBottomSheet

@Composable
fun ExploreScreen(
    onEventClick: (String) -> Unit,
    onMapClick: () -> Unit = {},
    viewModel: com.example.eventify.viewmodels.ExploreViewModelKMM = remember { AppModule.provideExploreViewModel() }
) {
    val events by viewModel.events.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var currentFilterState by remember { mutableStateOf(FilterState()) }

    Scaffold(
        containerColor = Color(0xFF0B0A12)
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // --- TOPO ---
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ExploreSearchBar(
                        query = searchQuery,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        modifier = Modifier.weight(1f)
                    )
                    FilledIconButton(
                        onClick = onMapClick,
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF151520)),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Map", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- FILTROS ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "Upcoming Events" else "Search Results",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Button(
                        onClick = { showFilters = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF151520), contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Filters")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- LISTA ---
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    if (events.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No events found.", color = Color.Gray)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            items(events) { event ->
                                ExploreEventCard(
                                    event = event,
                                    onClick = { onEventClick(event.id) },
                                    // LIGAÇÃO FINAL AQUI:
                                    onToggleFavorite = { viewModel.toggleFavorite(event.id) }
                                )
                            }
                        }
                    }
                }
            }

            if (showFilters) {
                FilterBottomSheet(
                    initialState = currentFilterState,
                    onApply = { newFilterState ->
                        currentFilterState = newFilterState
                        viewModel.updateFilters(newFilterState)
                        showFilters = false
                    },
                    onDismiss = { showFilters = false }
                )
            }
        }
    }
}

// --- COMPONENTES LOCAIS ---

@Composable
fun ExploreSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search events", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
        shape = CircleShape,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = Color.Gray,
            focusedContainerColor = Color(0xFF151520),
            unfocusedContainerColor = Color(0xFF151520),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun ExploreEventCard(
    event: Event,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit // <--- Novo callback
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))
    ) {
        Column {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.7f)
            )

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                InfoRow(icon = Icons.Default.CalendarMonth, text = event.dateTime)
                InfoRow(icon = Icons.Default.LocationOn, text = event.location)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val priceText = if (event.price == 0.0) "Free" else "$${event.price}"
                    val priceColor = if (event.price == 0.0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary

                    Text(priceText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = priceColor)

                    // Botão de Bookmark Reativo
                    Button(
                        onClick = onToggleFavorite, // <--- Chama o ViewModel
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        // Ícone muda se estiver salvo ou não
                        Icon(
                            imageVector = if (event.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (event.isSaved) "Saved" else "Save", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}