package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.Event
import com.example.eventify.ui.components.EventCard
import com.example.eventify.ui.components.IconButtonWithBadge

// =====================================================================
// TELA HOME (Conteúdo)
// =====================================================================

@Composable
fun HomeScreenContent(
    onEventClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    // Callbacks extras para a TopBar
    onSearchClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // 1. Injeção do ViewModel via KMM
    val viewModel = remember { AppModule.provideHomeViewModel() }

    // 2. Estados
    val featuredEvents by viewModel.featuredEvents.collectAsState()
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            HomeTopBar(
                onSearchClick = onSearchClick,
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color(0xFF0B0A12) // Fundo escuro global
    ) { innerPadding ->

        // Conteúdo com Scroll
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Seção Featured (Destaques) ---
            item {
                SectionHeader("Featured This Week")
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    // Skeleton para o Carrossel
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(2) { FeatureCardPlaceholder() }
                    }
                } else {
                    if (featuredEvents.isNotEmpty()) {
                        FeaturedEventsCarousel(
                            events = featuredEvents,
                            onEventClick = onEventClick
                        )
                    } else {
                        Text("No featured events right now.", color = Color.Gray)
                    }
                }
            }

            // --- Seção Upcoming (Próximos) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Upcoming Near You")
                    TextButton(onClick = onSeeAllClick) {
                        Text("See All", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (isLoading) {
                // Skeleton para a Lista Vertical
                items(3) {
                    EventCardPlaceholder()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                items(upcomingEvents) { event ->
                    // USANDO O TEU COMPONENTE EventCard
                    EventCard(
                        event = event,
                        onClick = onEventClick,
                        onSave = { /* Implementar lógica de salvar no ViewModel futuramente */ }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (upcomingEvents.isEmpty() && !isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No upcoming events found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// =====================================================================
// COMPONENTES LOCAIS (TopBar, Headers, FeatureCard)
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Text(
                "Eventify",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF0B0A12) // Mesma cor do fundo
        ),
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search events", tint = Color.White)
            }
            // Usando o teu componente IconButtonWithBadge
            IconButtonWithBadge(
                icon = Icons.Default.Notifications,
                badgeCount = 3, // Exemplo estático, depois podes ligar ao ViewModel
                onClick = onNotificationsClick
            )
            IconButton(onClick = onProfileClick) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = Color.White)
            }
        }
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Color.White
    )
}

@Composable
fun FeaturedEventsCarousel(
    events: List<Event>,
    onEventClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(end = 16.dp) // Espaço extra no fim do scroll
    ) {
        items(events) { event ->
            FeatureCard(event = event, onClick = { onEventClick(event.id) })
        }
    }
}

@Composable
fun FeatureCard(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(280.dp), // Largura de destaque
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))
    ) {
        Column {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = "Poster for ${event.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))

                // Exibe a data (string simples vinda do KMM)
                Text(
                    text = event.dateTime,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

// =====================================================================
// SKELETONS (Placeholders de Carregamento)
// =====================================================================

@Composable
fun FeatureCardPlaceholder() {
    Card(
        modifier = Modifier.width(280.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(Color.Gray.copy(alpha = 0.2f))
            )
            Column(Modifier.padding(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(24.dp).background(Color.Gray.copy(alpha = 0.2f)))
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp).background(Color.Gray.copy(alpha = 0.2f)))
            }
        }
    }
}

@Composable
fun EventCardPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(Color.Gray.copy(alpha = 0.2f))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp).background(Color.Gray.copy(alpha = 0.2f)))
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(20.dp).background(Color.Gray.copy(alpha = 0.2f)))
                Box(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).background(Color.Gray.copy(alpha = 0.2f)))
            }
        }
    }
}