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
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.Event
import kotlinx.serialization.InternalSerializationApi

// --- Configuração de Cores ---
private val BgDark = Color(0xFF0B0A12)
private val CardBg = Color(0xFF151520)
private val AccentPurple = Color(0xFF7B61FF)
private val TextWhite = Color.White
private val TextGray = Color(0xFF9CA3AF)
private val GreenGrowth = Color(0xFF00E096)

@OptIn(InternalSerializationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun OrganizerDashboardScreen(
    onCreateEventClick: () -> Unit,
    onEventClick: (String) -> Unit,
    onScanClick: () -> Unit,
    onEditEventClick: (String) -> Unit,
    onViewAttendeesClick: (String) -> Unit
) {
    // 1. Injeção do ViewModel
    val viewModel = remember { AppModule.provideOrganizerViewModel() }

    // 2. Coleta de Estado Reativo (Imports automáticos de getValue/setValue são essenciais aqui)
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    // 3. Dados Simulados para Estatísticas
    val totalRevenue = "$12,450"
    val revenueGrowth = "+5.2%"
    val registrations = "8,921"
    val regGrowth = "+12.1%"
    val upcomingCount = events.size.toString()
    val hostedCount = "23"

    Scaffold(
        containerColor = BgDark,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEventClick,
                containerColor = AccentPurple,
                contentColor = TextWhite,
                shape = CircleShape,
                modifier = Modifier.size(64.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Event", modifier = Modifier.size(32.dp))
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                OrganizerHeader(onScanClick = onScanClick)
            }

            item {
                StatsGrid(
                    revenue = totalRevenue, revGrowth = revenueGrowth,
                    registrations = registrations, regGrowth = regGrowth,
                    upcoming = upcomingCount, hosted = hostedCount
                )
            }

            // Cartão de Destaque (Próximo Evento)
            if (events.isNotEmpty()) {
                item {
                    val firstEvent = events.first()
                    HighlightEventCard(
                        event = firstEvent,
                        onClick = { onEventClick(firstEvent.id) },
                        onDeleteClick = { viewModel.deleteEvent(firstEvent.id) },
                        onEditClick = { onEditEventClick(firstEvent.id) },
                        onViewAttendeesClick = { onViewAttendeesClick(firstEvent.id) }
                    )
                }
            }

            item {
                Text(
                    text = "Recent Events",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }
            } else {
                // Lista de eventos restantes
                val recentList = if (events.size > 1) events.drop(1) else emptyList()

                if (recentList.isEmpty() && events.size <= 1) {
                    item {
                        Text("No other events found.", color = TextGray, modifier = Modifier.padding(vertical = 8.dp))
                    }
                } else {
                    items(recentList) { event ->
                        DashboardEventItem(
                            event = event,
                            onClick = { onEventClick(event.id) },
                            onDeleteClick = { viewModel.deleteEvent(event.id) },
                            onEditClick = { onEditEventClick(event.id) },
                            onViewAttendeesClick = { onViewAttendeesClick(event.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// --- Componentes Auxiliares ---

@Composable
fun OrganizerHeader(onScanClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Dashboard", style = MaterialTheme.typography.titleMedium, color = TextGray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Welcome back, Alex!", style = MaterialTheme.typography.headlineMedium, color = TextWhite, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onScanClick,
                colors = IconButtonDefaults.iconButtonColors(containerColor = CardBg)
            ) {
                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = AccentPurple)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(shape = CircleShape, modifier = Modifier.size(48.dp), color = CardBg) {
                Icon(Icons.Default.Person, contentDescription = "Profile", tint = TextGray, modifier = Modifier.padding(8.dp))
            }
        }
    }
}

@Composable
fun StatsGrid(revenue: String, revGrowth: String, registrations: String, regGrowth: String, upcoming: String, hosted: String) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(Modifier.weight(1f), Icons.Outlined.Paid, AccentPurple, "Total Revenue", revenue, "$revGrowth this month", GreenGrowth)
            StatCard(Modifier.weight(1f), Icons.Outlined.Group, AccentPurple, "Registrations", registrations, "$regGrowth this month", GreenGrowth)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCardSmall(Modifier.weight(1f), Icons.Outlined.Event, "Upcoming", upcoming)
            StatCardSmall(Modifier.weight(1f), Icons.Outlined.CalendarToday, "Hosted", hosted)
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, icon: ImageVector, iconColor: Color, title: String, value: String, subValue: String, subValueColor: Color) {
    Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium, color = TextGray)
            }
            Spacer(Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(Modifier.height(4.dp))
            Text(subValue, style = MaterialTheme.typography.labelSmall, color = subValueColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatCardSmall(modifier: Modifier, icon: ImageVector, title: String, value: String) {
    Card(modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = CardBg)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = AccentPurple, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium, color = TextWhite)
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextWhite)
        }
    }
}

@OptIn(InternalSerializationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HighlightEventCard(
    event: Event,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onViewAttendeesClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val gradient = Brush.horizontalGradient(colors = listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0)))

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.background(gradient).padding(24.dp)) {
            Column {
                Spacer(modifier = Modifier.height(40.dp))
                Text("Venue: ${event.locationName}", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(event.title, style = MaterialTheme.typography.headlineSmall, color = TextWhite, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (event.description.length > 80) event.description.take(80) + "..." else event.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextWhite.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("${event.maxCapacity}", color = TextWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Capacity", color = TextWhite.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = BgDark.copy(alpha = 0.5f)), shape = RoundedCornerShape(12.dp)) {
                        Text("Manage")
                    }
                }
            }

            // Menu de Opções
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "Options", tint = Color.White) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = CardBg) {
                    DropdownMenuItem(
                        text = { Text("Edit Event", color = Color.White) },
                        onClick = { showMenu = false; onEditClick() },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color.White) }
                    )
                    DropdownMenuItem(
                        text = { Text("Attendees", color = Color.White) },
                        onClick = { showMenu = false; onViewAttendeesClick() },
                        leadingIcon = { Icon(Icons.Default.People, null, tint = Color.White) }
                    )
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFFF3D71)) },
                        onClick = { showMenu = false; onDeleteClick() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF3D71)) }
                    )
                }
            }
        }
    }
}

@OptIn(InternalSerializationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DashboardEventItem(
    event: Event,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onViewAttendeesClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)).background(Color.DarkGray)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextWhite, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(event.dateTime.take(10), style = MaterialTheme.typography.bodySmall, color = TextGray)
                Spacer(modifier = Modifier.height(6.dp))
                Surface(color = GreenGrowth.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                    Text("Confirmed", color = GreenGrowth, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Outlined.MoreVert, "More", tint = TextGray) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }, containerColor = CardBg) {
                    DropdownMenuItem(
                        text = { Text("Edit Event", color = Color.White) },
                        onClick = { showMenu = false; onEditClick() },
                        leadingIcon = { Icon(Icons.Default.Edit, null, tint = Color.White) }
                    )
                    DropdownMenuItem(
                        text = { Text("Attendees", color = Color.White) },
                        onClick = { showMenu = false; onViewAttendeesClick() },
                        leadingIcon = { Icon(Icons.Default.People, null, tint = Color.White) }
                    )
                    HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFFF3D71)) },
                        onClick = { showMenu = false; onDeleteClick() },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color(0xFFFF3D71)) }
                    )
                }
            }
        }
    }
}