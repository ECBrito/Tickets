package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
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
import com.example.eventify.ui.components.EventLocationViewer
import com.example.eventify.ui.theme.EventifyTheme
import com.example.eventify.ui.viewmodels.EventDetailViewModel

@Composable
fun EventDetailScreen(
    eventId: String,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    viewModel: EventDetailViewModel = viewModel()
) {
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    val event by viewModel.event.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (event != null) {
                // Passamos o estado de registo e a função de clique
                BottomBar(
                    isRegistered = event!!.isRegistered,
                    onRsvpClick = { viewModel.toggleRsvp() }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (event == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Event not found",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onBackClick) { Text("Go Back") }
                }
            } else {
                EventContent(
                    event = event!!,
                    onBackClick = onBackClick,
                    onShareClick = onShareClick
                )
            }
        }
    }
}

@Composable
private fun EventContent(
    event: Event,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            HeroSection(
                event = event,
                onBackClick = onBackClick,
                onShareClick = onShareClick
            )
        }
        item { EventTabs() }
        item {
            DetailsContent(event)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ... (HeroSection, InfoRow, OrganizerChip, EventTabs, DetailsContent mantêm-se iguais ao anterior)
// ... Podes copiar as funções auxiliares do código anterior ou manter como está.
// ... Vou incluir apenas o BottomBar atualizado e o HeroSection para garantir contexto

@Composable
private fun HeroSection(
    event: Event,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
        AsyncImage(model = event.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Black.copy(0.6f), Color.Transparent, Color.Black.copy(0.9f)), startY = 0f, endY = 800f)))
        Row(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter).padding(16.dp, 48.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            IconButton(onClick = onBackClick) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White) }
            IconButton(onClick = onShareClick) { Icon(Icons.Default.Share, "Share", tint = Color.White) }
        }
        Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
            OrganizerChip(event.organizer)
            Spacer(Modifier.height(8.dp))
            Text(event.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
private fun OrganizerChip(organizer: String) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface.copy(0.8f), contentColor = MaterialTheme.colorScheme.onSurface) {
        Row(modifier = Modifier.padding(12.dp, 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.ConfirmationNumber, null, modifier = Modifier.size(14.dp))
            Text("By $organizer", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventTabs() {
    var selected by remember { mutableIntStateOf(0) }; val tabs = listOf("Details", "Attendees", "Comments")
    TabRow(selected, containerColor = MaterialTheme.colorScheme.background, contentColor = MaterialTheme.colorScheme.primary) {
        tabs.forEachIndexed { i, t -> Tab(selected == i, { selected = i }, text = { Text(t) }) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailsContent(event: Event) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val d = event.dateTime; val dateStr = "${d.dayOfWeek.name.take(3)}, ${d.dayOfMonth} ${d.month.name.take(3)} • ${d.hour}:00"
            InfoRow(Icons.Default.CalendarMonth, dateStr)
            val loc = if (event.location.contains(",")) "View on Map below" else event.location
            InfoRow(Icons.Default.LocationOn, loc)
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("About", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Text("Join us for an unforgettable experience at ${event.title}.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            val coords = try { val p = event.location.split(","); if (p.size == 2) Pair(p[0].trim().toDouble(), p[1].trim().toDouble()) else null } catch (e: Exception) { null }
            if (coords != null) {
                EventLocationViewer(coords.first, coords.second)
                Spacer(Modifier.height(4.dp))
                Text("Coordinates: ${event.location}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Box(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant), Alignment.Center) { Text(event.location, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Tags", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(event.category.name, if (event.price == 0.0) "Free" else "Paid").forEach { TagChip(it) }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        Text(text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground)
    }
}

@Composable
private fun TagChip(text: String) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), contentColor = MaterialTheme.colorScheme.onSurfaceVariant) {
        Text(text, Modifier.padding(16.dp, 8.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

// --- BOTTOM BAR ATUALIZADA ---
@Composable
private fun BottomBar(
    isRegistered: Boolean,
    onRsvpClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 16.dp
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Button(
                onClick = onRsvpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
                // Muda a cor se já estiver registado
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRegistered) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                    contentColor = if (isRegistered) MaterialTheme.colorScheme.onSecondaryContainer else Color.White
                )
            ) {
                // Muda o ícone e o texto
                Icon(
                    imageVector = if (isRegistered) Icons.Default.Check else Icons.Default.ConfirmationNumber,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = if (isRegistered) "Registered" else "RSVP Now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}