package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.Event
import com.example.eventify.ui.Screen // <--- IMPORTANTE: Adicionado para aceder às rotas
import kotlinx.datetime.LocalDateTime

// Cores específicas do design
private val BgDark = Color(0xFF0B0A12)
private val TextWhite = Color.White
private val TextGray = Color(0xFF9CA3AF)
private val AccentPurple = Color(0xFFD0BCFF)
private val AccentPurpleDark = Color(0xFF381E72)
private val ChipBg = Color(0xFF1E1E2C)

@Composable
fun EventDetailScreen(
    eventId: String,
    userId: String,
    navController: NavController
) {
    val viewModel = remember { AppModule.provideEventDetailViewModel(eventId) }
    val event by viewModel.event.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            event?.let { currentEvent ->
                BottomActionSection(
                    isRegistered = viewModel.isRegistered,
                    onRsvpClick = {
                        // LÓGICA CORRIGIDA AQUI:
                        if (viewModel.isRegistered) {
                            // Se já está inscrito, cancela a inscrição
                            viewModel.toggleRsvp()
                        } else {
                            // Se NÃO está inscrito, vai para o ecrã de COMPRA
                            navController.navigate(Screen.purchase(currentEvent.id))
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        if (isLoading || event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPurple)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Header
                HeaderSection(
                    event = event!!,
                    onBackClick = { navController.popBackStack() }
                )

                // 2. Info
                InfoSection(event = event!!)

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Tabs
                TabsSection()

                Spacer(modifier = Modifier.height(24.dp))

                // 4. About
                AboutSection(description = event!!.description)

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Location
                LocationMapSection()

                Spacer(modifier = Modifier.height(24.dp))

                // 6. Tags
                TagsSection(category = event!!.category, price = event!!.price)

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// --- SEÇÕES DA UI ---

@Composable
fun HeaderSection(event: Event, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
    ) {
        AsyncImage(
            model = event.imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, BgDark),
                        startY = 300f
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackClick,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
            }
            IconButton(
                onClick = { },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = TextWhite)
            }
        }

        Text(
            text = event.title,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        )
    }
}

@Composable
fun InfoSection(event: Event) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        // Data
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(40.dp).background(ChipBg, CircleShape).padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = formatDateTime(event.dateTime),
                style = MaterialTheme.typography.bodyLarge,
                color = TextWhite
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Localização
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = TextGray,
                modifier = Modifier.size(40.dp).background(ChipBg, CircleShape).padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = event.location,
                style = MaterialTheme.typography.bodyLarge,
                color = TextWhite
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Organizador Chip
        Row(
            modifier = Modifier
                .border(1.dp, TextGray.copy(alpha = 0.3f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AccentPurple,
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("E", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentPurpleDark)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Organized by Eventify Inc.",
                color = TextWhite,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun TabsSection() {
    val tabs = listOf("Details", "Attendees", "Comments")
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { selectedTab = index }
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (selectedTab == index) TextWhite else TextGray,
                        fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (selectedTab == index) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(3.dp)
                                .background(AccentPurple, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(1.dp))
        HorizontalDivider(color = TextGray.copy(alpha = 0.2f), thickness = 1.dp)
    }
}

@Composable
fun AboutSection(description: String) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("About this Event", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextGray,
            lineHeight = 24.sp
        )
    }
}

@Composable
fun LocationMapSection() {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2C2C3E))
        ) {
            // Placeholder visual
            AsyncImage(
                model = "https://maps.googleapis.com/maps/api/staticmap?center=Lisbon&zoom=13&size=600x300&maptype=roadmap&key=YOUR_KEY",
                contentDescription = "Map Preview",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().alpha(0.5f)
            )

            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.align(Alignment.Center).size(40.dp)
            )
        }
    }
}

@Composable
fun TagsSection(category: String, price: Double) {
    val tags = listOf(category, "Outdoor", "Festival", if (price == 0.0) "Free" else "Paid")

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Tags", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            tags.forEach { tag ->
                Box(
                    modifier = Modifier
                        .background(ChipBg, RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(tag, color = TextGray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun BottomActionSection(isRegistered: Boolean, onRsvpClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgDark)
            .padding(20.dp)
    ) {
        Button(
            onClick = onRsvpClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRegistered) Color.Gray else AccentPurple,
                contentColor = if (isRegistered) TextWhite else AccentPurpleDark
            )
        ) {
            Icon(
                imageVector = Icons.Default.ConfirmationNumber,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isRegistered) "CANCEL RSVP" else "RSVP NOW",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatDateTime(dateTime: String): String {
    return try {
        val parsed = LocalDateTime.parse(dateTime)
        val dayOfWeek = parsed.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        val month = parsed.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        val hour = parsed.hour.toString().padStart(2, '0')
        val minute = parsed.minute.toString().padStart(2, '0')

        "$dayOfWeek, $month ${parsed.dayOfMonth} • $hour:$minute"
    } catch (e: Exception) {
        dateTime
    }
}