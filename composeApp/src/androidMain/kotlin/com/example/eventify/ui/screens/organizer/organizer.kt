package com.example.eventify.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.eventify.model.Event
import com.example.eventify.ui.theme.EventifyTheme
import com.example.eventify.ui.viewmodels.OrganizerViewModel

// Modelo de dados para estatísticas
data class OrganizerStat(
    val title: String,
    val value: String,
    val trend: String? = null,
    val isPositive: Boolean = true,
    val icon: ImageVector
)

@Composable
fun OrganizerDashboardScreen(
    onCreateEventClick: () -> Unit,
    onEventClick: (String) -> Unit,
    viewModel: OrganizerViewModel = viewModel() // Injeção do ViewModel
) {
    // Coletar estados
    val stats by viewModel.stats.collectAsState()
    val recentEvents by viewModel.recentEvents.collectAsState()

    // Recarregar dados sempre que a tela aparece (para atualizar após criar evento)
    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    Scaffold(
        containerColor = Color(0xFF0B0A12), // Fundo escuro (Nocturnal)
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
            // 1. Header
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OrganizerHeader()
            }

            // 2. Stats Grid (Dinâmico)
            if (stats.isNotEmpty()) {
                item {
                    StatsGrid(stats)
                }
            }

            // 3. Banner de Destaque
            item {
                NextUpCard(onManageClick = { /* Ação futura */ })
            }

            // 4. Recent Events List (Dinâmico)
            item {
                Text(
                    "Recent Events",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (recentEvents.isEmpty()) {
                item {
                    Text("No events created yet.", color = Color.Gray)
                }
            } else {
                items(recentEvents) { event ->
                    RecentEventItem(event, onClick = { onEventClick(event.id) })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp)) // Espaço para o FAB
            }
        }
    }
}

// --- Componentes de Suporte ---

@Composable
fun OrganizerHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Welcome back!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Surface(
            shape = CircleShape,
            modifier = Modifier.size(40.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            AsyncImage(
                model = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=500",
                contentDescription = "Profile",
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun StatsGrid(stats: List<OrganizerStat>) {
    // Garante que temos pelo menos 4 estatísticas para preencher a grid
    if (stats.size >= 4) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OrganizerStatsCard(stat = stats[0], modifier = Modifier.weight(1f))
                OrganizerStatsCard(stat = stats[1], modifier = Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OrganizerStatsCard(stat = stats[2], modifier = Modifier.weight(1f))
                OrganizerStatsCard(stat = stats[3], modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun OrganizerStatsCard(stat: OrganizerStat, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = stat.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stat.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stat.value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            if (stat.trend != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stat.trend,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (stat.isPositive) Color(0xFF4CAF50) else Color(0xFFFF5252)
                )
            }
        }
    }
}

@Composable
fun NextUpCard(onManageClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151520)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
                        )
                    )
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Text("Next Up: 25 Dec 2024", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Nocturnal Tech Summit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text("A deep dive into the future of nighttime technology.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onManageClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Manage Event")
                }
            }
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
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1
                )
                Text(
                    text = event.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1
                )
            }

            IconButton(onClick = { }) {
                Icon(Icons.Default.MoreVert, contentDescription = "More", tint = Color.Gray)
            }
        }
    }
}

@Preview
@Composable
fun OrganizerDashboardPreview() {
    EventifyTheme(darkTheme = true) {
        OrganizerDashboardScreen(onCreateEventClick = {}, onEventClick = {})
    }
}