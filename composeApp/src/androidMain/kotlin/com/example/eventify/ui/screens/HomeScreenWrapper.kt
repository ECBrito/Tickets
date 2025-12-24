package com.example.eventify.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.Event
import com.example.eventify.model.EventCategory
import com.example.eventify.ui.components.IconButtonWithBadge
import com.example.eventify.ui.utils.getCurrentLocation
import com.example.eventify.viewmodels.HomeViewModelKMM
import androidx.navigation.NavController
import kotlinx.serialization.InternalSerializationApi

private val AccentPurple = Color(0xFF7B61FF)
private val ChipBg = Color(0xFF1E1E2C)

@OptIn(ExperimentalSharedTransitionApi::class, InternalSerializationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    val viewModel = remember { AppModule.provideHomeViewModel() }
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (granted) {
            getCurrentLocation(context) { lat, lon ->
                viewModel.updateUserLocation(lat, lon)
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    HomeScreenContent(
        viewModel = viewModel,
        onEventClick = { eventId -> navController.navigate("eventDetail/$eventId") },
        onSeeAllClick = { },
        onSearchClick = { },
        onNotificationsClick = { },
        onProfileClick = { navController.navigate("profile") },
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, InternalSerializationApi::class)
@Composable
fun HomeScreenContent(
    viewModel: HomeViewModelKMM,
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
        containerColor = Color(0xFF0B0A12)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(16.dp)) }

            item {
                SectionHeader("Featured This Week")
                Spacer(modifier = Modifier.height(16.dp))
                if (isLoading) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) { items(2) { FeatureCardPlaceholder() } }
                } else if (featuredEvents.isNotEmpty()) {
                    FeaturedEventsCarousel(featuredEvents, onEventClick, animatedVisibilityScope, sharedTransitionScope)
                } else {
                    Text("Check out upcoming events below!", color = Color.Gray)
                }
            }

            item { CategoryFilterSection(selectedCategory) { viewModel.selectCategory(it) } }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    SectionHeader("Upcoming Events")
                    TextButton(onClick = onSeeAllClick) { Text("See All", color = AccentPurple) }
                }
            }

            if (isLoading) {
                items(3) { EventCardPlaceholder(); Spacer(modifier = Modifier.height(12.dp)) }
            } else if (upcomingEvents.isEmpty()) {
                item { Box(Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("No events found nearby.", color = Color.Gray) } }
            } else {
                items(upcomingEvents) { event ->
                    EventCard(event, onEventClick, { viewModel.toggleSave(event.id) }, animatedVisibilityScope, sharedTransitionScope)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, InternalSerializationApi::class)
@Composable
fun FeaturedEventsCarousel(events: List<Event>, onEventClick: (String) -> Unit, animatedVisibilityScope: AnimatedVisibilityScope, sharedTransitionScope: SharedTransitionScope) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp), contentPadding = PaddingValues(end = 16.dp)) {
        items(events) { event ->
            FeatureCard(event, { onEventClick(event.id) }, animatedVisibilityScope, sharedTransitionScope)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, InternalSerializationApi::class)
@Composable
fun EventCard(event: Event, onClick: (String) -> Unit, onSave: (String) -> Unit, animatedVisibilityScope: AnimatedVisibilityScope, sharedTransitionScope: SharedTransitionScope) {
    with(sharedTransitionScope) {
        Card(onClick = { onClick(event.id) }, modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))) {
            Column {
                AsyncImage(model = event.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(160.dp).sharedElement(rememberSharedContentState(key = "image-${event.id}"), animatedVisibilityScope).clip(MaterialTheme.shapes.medium))
                Column(Modifier.padding(16.dp)) {
                    Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                    Spacer(Modifier.height(4.dp))
                    Text(event.dateTime.split("T").firstOrNull() ?: event.dateTime, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, InternalSerializationApi::class)
@Composable
fun FeatureCard(event: Event, onClick: () -> Unit, animatedVisibilityScope: AnimatedVisibilityScope, sharedTransitionScope: SharedTransitionScope) {
    with(sharedTransitionScope) {
        Card(onClick = onClick, modifier = Modifier.width(280.dp), shape = MaterialTheme.shapes.medium, colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))) {
            Column {
                AsyncImage(model = event.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(160.dp).sharedElement(rememberSharedContentState(key = "image-${event.id}"), animatedVisibilityScope).clip(MaterialTheme.shapes.medium))
                Column(Modifier.padding(16.dp)) {
                    Text(event.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
                    Spacer(Modifier.height(4.dp))
                    Text(event.dateTime.split("T").firstOrNull() ?: event.dateTime, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun CategoryFilterSection(selectedCategory: EventCategory?, onCategorySelected: (EventCategory?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item { CategoryChip("All", selectedCategory == null) { onCategorySelected(null) } }
        items(EventCategory.entries) { cat ->
            CategoryChip(cat.name.lowercase().replaceFirstChar { it.titlecase() }, selectedCategory == cat) { onCategorySelected(cat) }
        }
    }
}

@Composable
fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(color = if (isSelected) AccentPurple else ChipBg, shape = RoundedCornerShape(50), modifier = Modifier.clickable(onClick = onClick).border(1.dp, if (isSelected) Color.Transparent else Color.Gray.copy(0.3f), RoundedCornerShape(50))) {
        Text(label, color = if (isSelected) Color.White else Color.Gray, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(onSearchClick: () -> Unit, onNotificationsClick: () -> Unit, onProfileClick: () -> Unit) {
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