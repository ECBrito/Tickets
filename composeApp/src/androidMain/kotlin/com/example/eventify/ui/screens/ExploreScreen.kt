package com.example.eventify.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.Event
import com.example.eventify.model.FilterState
import com.example.eventify.ui.components.FilterBottomSheet
import kotlinx.serialization.InternalSerializationApi

@OptIn(ExperimentalSharedTransitionApi::class, InternalSerializationApi::class)
@Composable
fun ExploreScreen(
    onEventClick: (String) -> Unit,
    onMapClick: () -> Unit = {},
    viewModel: com.example.eventify.viewmodels.ExploreViewModelKMM = remember { AppModule.provideExploreViewModel() },
    animatedVisibilityScope: AnimatedVisibilityScope? = null,
    sharedTransitionScope: SharedTransitionScope? = null
) {
    val events by viewModel.events.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showFilters by remember { mutableStateOf(false) }
    var currentFilterState by remember { mutableStateOf(FilterState()) }

    Scaffold(
        containerColor = Color(0xFF0B0A12)
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
                Spacer(modifier = Modifier.height(16.dp))

                // TOPO: Pesquisa + Botão de Mapa
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
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Map, contentDescription = "Ver Mapa", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // CABEÇALHO + FILTROS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (searchQuery.isEmpty()) "Próximos Eventos" else "Resultados",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Button(
                        onClick = { showFilters = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF151520))
                    ) {
                        Icon(Icons.Default.Tune, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Filtros")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFFD0BCFF))
                    }
                } else {
                    if (events.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nenhum evento encontrado.", color = Color.Gray)
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
                    onApply = { newFilter ->
                        currentFilterState = newFilter
                        viewModel.updateFilters(newFilter)
                        showFilters = false
                    },
                    onDismiss = { showFilters = false }
                )
            }
        }
    }
}

@Composable
fun ExploreSearchBar(query: String, onQueryChange: (String) -> Unit, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier,
        placeholder = { Text("Pesquisar eventos ou locais", color = Color.Gray) },
        leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
        shape = CircleShape,
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color(0xFF151520),
            unfocusedContainerColor = Color(0xFF151520),
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@OptIn(InternalSerializationApi::class)
@Composable
fun ExploreEventCard(event: Event, onClick: () -> Unit, onToggleFavorite: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))
    ) {
        Column {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1.8f)
            )

            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(event.locationName, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val priceLabel = if (event.price == 0.0) "Grátis" else "${event.price}€"
                    Text(priceLabel, color = if (event.price == 0.0) Color(0xFF00E096) else Color(0xFFD0BCFF), fontWeight = FontWeight.Bold)

                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (event.isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = null,
                            tint = Color(0xFFD0BCFF)
                        )
                    }
                }
            }
        }
    }
}