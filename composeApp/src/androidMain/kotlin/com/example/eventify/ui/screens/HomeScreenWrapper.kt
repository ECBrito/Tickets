package com.example.eventify.ui.screens

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eventify.model.Event
import com.example.eventify.model.EventCategory
import com.example.eventify.ui.components.IconButtonWithBadge
import com.example.eventify.viewmodels.HomeViewModelKMM
import kotlinx.serialization.InternalSerializationApi
import kotlin.math.*

private val AccentPurple = Color(0xFF7B61FF)
private val ChipBg = Color(0xFF1E1E2C)
private val BgDark = Color(0xFF0B0A12)

@OptIn(ExperimentalSharedTransitionApi::class, InternalSerializationApi::class)
@Composable
fun HomeScreenContent(
    viewModel: HomeViewModelKMM,
    userLat: Double?,
    userLon: Double?,
    onEventClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    val featuredEvents by viewModel.featuredEvents.collectAsState()
    val upcomingEvents by viewModel.upcomingEvents.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = { HomeTopBar(onSearchClick, onNotificationsClick, onProfileClick) },
        containerColor = BgDark
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // DESTAQUES
            item {
                SectionHeader("Destaques da Semana")
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoading) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) { items(2) { FeatureCardPlaceholder() } }
                } else if (featuredEvents.isNotEmpty()) {
                    FeaturedEventsCarousel(featuredEvents, userLat ?: 0.0, userLon ?: 0.0, onEventClick, animatedVisibilityScope, sharedTransitionScope)
                }
            }

            // FILTROS
            item { CategoryFilterSection(selectedCategory) { viewModel.selectCategory(it) } }

            // PRÓXIMOS
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader("Próximos Eventos")
                    TextButton(onClick = onSeeAllClick) { Text("Ver Todos", color = AccentPurple) }
                }
            }

            if (isLoading) {
                items(3) { EventCardPlaceholder(); Spacer(modifier = Modifier.height(12.dp)) }
            } else if (upcomingEvents.isEmpty()) {
                item { Box(Modifier.padding(40.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Sem eventos por perto.", color = Color.Gray) } }
            } else {
                items(upcomingEvents) { event ->
                    EventCard(event, userLat ?: 0.0, userLon ?: 0.0, onEventClick, { viewModel.toggleSave(event.id) }, animatedVisibilityScope, sharedTransitionScope)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// --- COMPONENTES AUXILIARES ---

@OptIn(ExperimentalSharedTransitionApi::class, InternalSerializationApi::class)
@Composable
fun EventCard(event: Event, userLat: Double, userLon: Double, onClick: (String) -> Unit, onSave: (String) -> Unit, animatedVisibilityScope: AnimatedVisibilityScope, sharedTransitionScope: SharedTransitionScope) {
    val distance = calculateDistanceKm(userLat, userLon, event.latitude, event.longitude)
    with(sharedTransitionScope) {
        Card(onClick = { onClick(event.id) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))) {
            Column {
                Box {
                    AsyncImage(model = event.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(160.dp).sharedElement(rememberSharedContentState(key = "image-${event.id}"), animatedVisibilityScope).clip(RoundedCornerShape(16.dp)))
                    if (distance > 0.1) {
                        Surface(color = Color.Black.copy(0.7f), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp)) {
                            Row(Modifier.padding(8.dp, 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = AccentPurple, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("${"%.1f".format(distance)} km", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Column(Modifier.padding(16.dp)) {
                    Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(event.locationName, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, InternalSerializationApi::class)
@Composable
fun FeaturedEventsCarousel(events: List<Event>, userLat: Double, userLon: Double, onEventClick: (String) -> Unit, animatedVisibilityScope: AnimatedVisibilityScope, sharedTransitionScope: SharedTransitionScope) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        items(events) { event ->
            FeatureCard(event, userLat, userLon, { onEventClick(event.id) }, animatedVisibilityScope, sharedTransitionScope)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, InternalSerializationApi::class)
@Composable
fun FeatureCard(event: Event, userLat: Double, userLon: Double, onClick: () -> Unit, animatedVisibilityScope: AnimatedVisibilityScope, sharedTransitionScope: SharedTransitionScope) {
    val distance = calculateDistanceKm(userLat, userLon, event.latitude, event.longitude)
    with(sharedTransitionScope) {
        Card(onClick = onClick, modifier = Modifier.width(280.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))) {
            Column {
                Box {
                    AsyncImage(model = event.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(160.dp).sharedElement(rememberSharedContentState(key = "image-${event.id}"), animatedVisibilityScope).clip(RoundedCornerShape(16.dp)))
                    if (distance > 0.1) {
                        Surface(color = Color.Black.copy(0.6f), shape = RoundedCornerShape(8.dp), modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                            Text("${"%.1f".format(distance)} km", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(4.dp))
                        }
                    }
                }
                Column(Modifier.padding(16.dp)) {
                    Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(event.locationName, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun CategoryFilterSection(selectedCategory: EventCategory?, onCategorySelected: (EventCategory?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item { CategoryChip("Todos", selectedCategory == null) { onCategorySelected(null) } }
        items(EventCategory.entries) { cat ->
            val label = when(cat.name) {
                "MUSIC" -> "Música"
                "TECH" -> "Tech"
                "ART", "ARTS" -> "Arte"
                "FOOD" -> "Gastronomia"
                "SPORT", "SPORTS" -> "Desporto"
                else -> cat.name.lowercase().replaceFirstChar { it.titlecase() }
            }
            CategoryChip(label, selectedCategory == cat) { onCategorySelected(cat) }
        }
    }
}

@Composable
fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(color = if (isSelected) AccentPurple else ChipBg, shape = RoundedCornerShape(50), modifier = Modifier.clickable(onClick = onClick).border(1.dp, if (isSelected) Color.Transparent else Color.Gray.copy(0.3f), RoundedCornerShape(50))) {
        Text(label, color = if (isSelected) Color.White else Color.Gray, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onSearchClick: () -> Unit, onNotificationsClick: () -> Unit, onProfileClick: () -> Unit) {
    TopAppBar(
        title = { Text("Eventify", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 24.sp) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = BgDark),
        actions = {
            IconButton(onClick = onSearchClick) { Icon(Icons.Default.Search, null, tint = Color.White) }
            IconButtonWithBadge(icon = Icons.Default.Notifications, badgeCount = 3, onClick = onNotificationsClick)
            IconButton(onClick = onProfileClick) { Icon(Icons.Default.AccountCircle, null, tint = Color.White) }
        }
    )
}

@Composable
fun SectionHeader(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
}

fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    if (lat1 == 0.0 || lat2 == 0.0) return 0.0
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}

@Composable fun FeatureCardPlaceholder() { Card(Modifier.width(280.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color(0xFF151520))) { Box(Modifier.fillMaxWidth().height(160.dp).background(Color.Gray.copy(0.1f))) } }
@Composable fun EventCardPlaceholder() { Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(Color(0xFF151520))) { Box(Modifier.fillMaxWidth().height(100.dp).background(Color.Gray.copy(0.1f))) } }