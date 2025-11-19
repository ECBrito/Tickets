package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.eventify.model.Event
import com.example.eventify.model.FilterState
import com.example.eventify.ui.components.FilterBottomSheet
import com.example.eventify.ui.theme.EventifyTheme
import com.example.eventify.ui.viewmodels.ExploreViewModel
import kotlinx.datetime.LocalDateTime

// --- Composable Principal da Tela ---
@Composable
fun ExploreScreen(
    onEventClick: (String) -> Unit,
    viewModel: ExploreViewModel = viewModel() // Injeção do ViewModel
) {
    // Estados da UI
    var showFilters by remember { mutableStateOf(false) }

    // Estados observados do ViewModel
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredEvents by viewModel.filteredEvents.collectAsState() // A lista dinâmica filtrada

    // Estado local para o modal de filtros
    var currentFilterState by remember { mutableStateOf(FilterState()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Topo: Barra de Pesquisa
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExploreSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) }, // Liga ao ViewModel
                    modifier = Modifier.weight(1f)
                )
                // Botão de Mapa
                FilledIconButton(
                    onClick = { /* TODO: Alternar para Mapa */ },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Map, contentDescription = "Map View")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Cabeçalho da Lista e Botão de Filtro
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (searchQuery.isEmpty()) "Upcoming Events" else "Search Results",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                // Botão de Filtros
                Button(
                    onClick = { showFilters = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Filters")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Lista de Eventos (Dinâmica - Usa filteredEvents)
            if (filteredEvents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(bottom = 80.dp), contentAlignment = Alignment.Center) {
                    Text("No events found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // AQUI É O PONTO CHAVE: Usamos 'items(filteredEvents)'
                    items(filteredEvents) { event ->
                        ExploreEventCard(
                            event = event,
                            onClick = { onEventClick(event.id) }
                        )
                    }
                }
            }
        }

        // 4. Modal de Filtros
        if (showFilters) {
            FilterBottomSheet(
                initialState = currentFilterState,
                onApply = { newFilterState ->
                    currentFilterState = newFilterState
                    viewModel.onFilterApply(newFilterState) // Envia filtros para o ViewModel
                },
                onDismiss = { showFilters = false }
            )
        }
    }
}

// --- Componentes de Suporte (ExploreSearchBar, ExploreEventCard, InfoRow) ---
// (Mantém o código destes componentes que já tinhas e que estavam corretos)

@Composable
fun ExploreSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Search events or venues") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
        shape = CircleShape,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
fun ExploreEventCard(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = "Event Poster",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.7f)
            )

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                // Formatação simples de data
                val dateStr = "${event.dateTime.dayOfWeek.name.take(3)}, ${event.dateTime.dayOfMonth} ${event.dateTime.month.name.take(3)} • ${event.dateTime.hour}:00"
                InfoRow(icon = Icons.Default.CalendarMonth, text = dateStr)
                InfoRow(icon = Icons.Default.LocationOn, text = event.location)

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (event.price == 0.0) "Free" else "$${event.price.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (event.price == 0.0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = { /* Lógica de bookmark */ },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Bookmark", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}