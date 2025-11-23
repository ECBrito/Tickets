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
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Paid
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
import com.example.eventify.ui.theme.EventifyTheme
import com.example.eventify.viewmodels.OrganizerViewModel

// --- Theme Colors from Image ---
private val BgDark = Color(0xFF0B0A12)
private val CardBg = Color(0xFF151520)
private val AccentPurple = Color(0xFF7B61FF) // Button Purple
private val TextWhite = Color.White
private val TextGray = Color(0xFF9CA3AF)
private val GreenGrowth = Color(0xFF00E096)

@Composable
fun OrganizerDashboardScreen(
    onCreateEventClick: () -> Unit,
    onEventClick: (String) -> Unit
) {
    val viewModel = remember { AppModule.provideOrganizerViewModel() }
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Mock Stats Data (In a real app, this comes from ViewModel)
    val totalRevenue = "$12,450"
    val revenueGrowth = "+5.2%"
    val registrations = "8,921"
    val regGrowth = "+12.1%"
    val upcomingCount = events.size.toString() // Dynamic
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
            // 1. Header
            item {
                Spacer(modifier = Modifier.height(20.dp))
                OrganizerHeader()
            }

            // 2. Stats Grid
            item {
                StatsGrid(
                    revenue = totalRevenue,
                    revGrowth = revenueGrowth,
                    registrations = registrations,
                    regGrowth = regGrowth,
                    upcoming = upcomingCount,
                    hosted = hostedCount
                )
            }

            // 3. Highlight Card (Next Big Event)
            if (events.isNotEmpty()) {
                item {
                    // Pick the first event as "Next Up" for demo purposes
                    HighlightEventCard(event = events.first(), onClick = { onEventClick(events.first().id) })
                }
            }

            // 4. Recent Events Header
            item {
                Text(
                    text = "Recent Events",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite
                )
            }

            // 5. Recent Events List
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }
            } else {
                // Skip the first one if shown in Highlight, or show all
                val recentEvents = if (events.isNotEmpty()) events.drop(1) else emptyList()

                if (recentEvents.isEmpty() && events.size <= 1) {
                    item { Text("No recent activity.", color = TextGray) }
                } else {
                    items(recentEvents) { event ->
                        DashboardEventItem(event = event, onClick = { onEventClick(event.id) })
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

// --- COMPONENTES ---

@Composable
fun OrganizerHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Dashboard", style = MaterialTheme.typography.titleMedium, color = TextWhite, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Welcome back, Alex!", style = MaterialTheme.typography.headlineMedium, color = TextWhite, fontWeight = FontWeight.Bold)
        }
        Surface(
            shape = CircleShape,
            modifier = Modifier.size(48.dp),
            color = CardBg
        ) {
            // Mock Avatar
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = TextGray,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun StatsGrid(
    revenue: String, revGrowth: String,
    registrations: String, regGrowth: String,
    upcoming: String, hosted: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Row 1
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Paid,
                iconColor = Color(0xFF7B61FF), // Purple icon
                title = "Total Revenue",
                value = revenue,
                subValue = "$revGrowth this month",
                subValueColor = GreenGrowth
            )
            StatCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Group,
                iconColor = Color(0xFF7B61FF),
                title = "Registrations",
                value = registrations,
                subValue = "$regGrowth this month",
                subValueColor = GreenGrowth
            )
        }
        // Row 2
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCardSmall(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Event,
                title = "Upcoming",
                value = upcoming
            )
            StatCardSmall(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.CalendarToday,
                title = "Hosted",
                value = hosted
            )
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    subValue: String,
    subValueColor: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium, color = TextGray)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextWhite)
            Spacer(modifier = Modifier.height(4.dp))
            Text(subValue, style = MaterialTheme.typography.bodySmall, color = subValueColor, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StatCardSmall(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.bodyMedium, color = TextWhite)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = TextWhite)
        }
    }
}

@Composable
fun HighlightEventCard(event: Event, onClick: () -> Unit) {
    // Gradient Background imitating the image
    val gradient = Brush.horizontalGradient(
        colors = listOf(
            Color(0xFF8E2DE2), // Purple
            Color(0xFF4A00E0)  // Dark Blue
        )
    )

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent), // Transparent to show Box gradient
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .background(gradient)
                .padding(20.dp)
        ) {
            Column {
                // Placeholder image area or gradient area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp) // Top abstract area
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Next Up: ${event.dateTime.take(10)}", color = AccentPurple, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(4.dp))
                Text(event.title, style = MaterialTheme.typography.headlineSmall, color = TextWhite, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    event.description.take(80) + "...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("1,200/2,000", color = TextWhite, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Registered", color = TextGray, style = MaterialTheme.typography.bodySmall)
                    }

                    Button(
                        onClick = onClick,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Manage Event")
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardEventItem(event: Event, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Event Image
            AsyncImage(
                model = event.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.DarkGray)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextWhite, maxLines = 1)
                Spacer(modifier = Modifier.height(4.dp))
                Text(event.dateTime.take(10), style = MaterialTheme.typography.bodySmall, color = TextGray)
                Spacer(modifier = Modifier.height(6.dp))

                // Status Pill (Mock logic)
                val status = "Live" // Mock status
                val statusColor = Color(0xFF00E096) // Green

                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = status,
                        color = statusColor,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            IconButton(onClick = { /* Menu action */ }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "More", tint = TextGray)
            }
        }
    }
}