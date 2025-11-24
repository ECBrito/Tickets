package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.Event
import com.example.eventify.model.EventCategory // <--- Importante
import com.example.eventify.ui.components.EventCard
import com.example.eventify.ui.components.IconButtonWithBadge

// Cores
private val AccentPurple = Color(0xFF7B61FF)
private val ChipBg = Color(0xFF1E1E2C)

@Composable
fun HomeScreenContent(
    onEventClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    onSearchClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val viewModel = remember { AppModule.provideHomeViewModel() }

    val featuredEvents by viewModel.featuredEvents.collectAsState()
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState() // <--- Estado da Categoria
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            HomeTopBar(
                onSearchClick = onSearchClick,
                onNotificationsClick = onNotificationsClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = Color(0xFF0B0A12)
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            // --- 1. Featured (Destaques) ---
            item {
                SectionHeader("Featured This Week")
                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
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
                        Text("No featured events.", color = Color.Gray)
                    }
                }
            }

            // --- 2. CATEGORIAS (NOVO) ---
            item {
                CategoryFilterSection(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )
            }

            // --- 3. Upcoming (Próximos - Filtrado) ---
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Upcoming Events")
                    TextButton(onClick = onSeeAllClick) {
                        Text("See All", color = AccentPurple)
                    }
                }
            }

            if (isLoading) {
                items(3) {
                    EventCardPlaceholder()
                    Spacer(modifier = Modifier.height(12.dp))
                }
            } else {
                if (upcomingEvents.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No events found for this category.", color = Color.Gray)
                        }
                    }
                } else {
                    items(upcomingEvents) { event ->
                        EventCard(
                            event = event,
                            onClick = onEventClick,
                            onSave = { viewModel.toggleSave(event.id) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

// --- NOVO COMPONENTE: Filtros de Categoria ---
@Composable
fun CategoryFilterSection(
    selectedCategory: EventCategory?,
    onCategorySelected: (EventCategory?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Opção "All"
        item {
            CategoryChip(
                label = "All",
                isSelected = selectedCategory == null,
                onClick = { onCategorySelected(null) }
            )
        }

        // Opções do Enum
        items(EventCategory.entries) { category ->
            CategoryChip(
                label = category.name.lowercase().replaceFirstChar { it.titlecase() },
                isSelected = selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) AccentPurple else ChipBg,
        shape = RoundedCornerShape(50), // Redondo
        modifier = Modifier
            .clickable(onClick = onClick)
            .border(
                width = 1.dp,
                color = if (isSelected) Color.Transparent else Color.Gray.copy(alpha = 0.3f),
                shape = RoundedCornerShape(50)
            )
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.Gray,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
        )
    }
}

// --- OUTROS COMPONENTES (Mantidos) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    TopAppBar(
        title = { Text("Eventify", fontWeight = FontWeight.Bold, color = Color.White) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0A12)),
        actions = {
            IconButton(onClick = onSearchClick) { Icon(Icons.Default.Search, null, tint = Color.White) }
            IconButtonWithBadge(icon = Icons.Default.Notifications, badgeCount = 0, onClick = onNotificationsClick)
            IconButton(onClick = onProfileClick) { Icon(Icons.Default.AccountCircle, null, tint = Color.White) }
        }
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
}

@Composable
fun FeaturedEventsCarousel(events: List<Event>, onEventClick: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(end = 16.dp)) {
        items(events) { event -> FeatureCard(event, onClick = { onEventClick(event.id) }) }
    }
}

@Composable
fun FeatureCard(event: Event, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.width(280.dp), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))) {
        Column {
            AsyncImage(model = event.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(160.dp).clip(MaterialTheme.shapes.medium))
            Column(Modifier.padding(16.dp)) {
                Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(event.dateTime.take(10), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            }
        }
    }
}

@Composable
fun FeatureCardPlaceholder() {
    Card(modifier = Modifier.width(280.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))) {
        Box(modifier = Modifier.fillMaxWidth().height(160.dp).background(Color.Gray.copy(alpha = 0.2f)))
    }
}

@Composable
fun EventCardPlaceholder() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))) {
        Box(modifier = Modifier.height(80.dp).background(Color.Gray.copy(alpha = 0.2f)))
    }
}