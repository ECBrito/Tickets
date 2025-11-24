package com.example.eventify.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.Comment
import com.example.eventify.model.Event
import com.example.eventify.ui.Screen
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
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Estado da Tab (0=Details, 1=Attendees, 2=Comments)
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            event?.let { currentEvent ->
                BottomActionSection(
                    isRegistered = viewModel.isRegistered,
                    onRsvpClick = {
                        if (viewModel.isRegistered) {
                            viewModel.toggleRsvp()
                        } else {
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
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    // 1. Header
                    item {
                        HeaderSection(
                            event = event!!,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    // 2. Info
                    item {
                        InfoSection(event = event!!)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // 3. Tabs
                    item {
                        TabsSection(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // 4. CONTEÚDO DA TAB
                    when (selectedTab) {
                        0 -> { // Details Tab
                            item {
                                Column {
                                    AboutSection(description = event!!.description)
                                    Spacer(modifier = Modifier.height(24.dp))

                                    // Passamos o nome do local para o componente do mapa
                                    LocationMapSection(locationName = event!!.location)

                                    Spacer(modifier = Modifier.height(24.dp))
                                    TagsSection(category = event!!.category, price = event!!.price)
                                    Spacer(modifier = Modifier.height(40.dp))
                                }
                            }
                        }
                        1 -> { // Attendees Tab
                            item {
                                Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                    Text("Attendees list coming soon...", color = TextGray)
                                }
                            }
                        }
                        2 -> { // Comments Tab
                            item {
                                CommentInputSection(onSendComment = { text -> viewModel.sendComment(text) })
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            if (comments.isEmpty()) {
                                item {
                                    Text("No comments yet. Be the first!", color = TextGray, modifier = Modifier.padding(horizontal = 20.dp))
                                }
                            } else {
                                items(comments) { comment ->
                                    CommentItem(comment)
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTES COM INTENTS ---

@Composable
fun HeaderSection(event: Event, onBackClick: () -> Unit) {
    val context = LocalContext.current // Necessário para iniciar Intents

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

            // --- BOTÃO DE PARTILHA (SHARE) ---
            IconButton(
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, "Hey! Check out this event: ${event.title} at ${event.location}. Join me on Eventify!")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Share Event")
                    context.startActivity(shareIntent)
                },
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
fun LocationMapSection(locationName: String) {
    val context = LocalContext.current // Necessário para iniciar Intents

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2C2C3E))
                .clickable {
                    // --- ABRE O MAPA ---
                    // Cria um URI "geo:0,0?q=NomeDoLocal"
                    val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(locationName)}")
                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                    // Tenta abrir diretamente a app de mapas do Google se disponível, senão deixa o sistema escolher
                    mapIntent.setPackage("com.google.android.apps.maps")

                    try {
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        // Fallback: Tenta abrir no browser ou outra app de mapas sem forçar pacote
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/?q=${Uri.encode(locationName)}"))
                        context.startActivity(browserIntent)
                    }
                }
        ) {
            // Placeholder visual (Imagem estática ou ícone)
            // Se tiveres API Key, podes usar a URL do Static Maps API. Se não, fica um placeholder bonito.
            AsyncImage(
                model = "https://maps.googleapis.com/maps/api/staticmap?center=${locationName}&zoom=14&size=600x300&maptype=roadmap&key=YOUR_API_KEY_HERE",
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

            Text(
                "Tap to open Maps",
                color = TextWhite,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)
            )
        }
    }
}

// --- RESTANTES COMPONENTES (Mantidos iguais) ---

@Composable
fun InfoSection(event: Event) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
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
fun TabsSection(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Details", "Attendees", "Comments")

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onTabSelected(index) }
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
fun CommentInputSection(onSendComment: (String) -> Unit) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Write a comment...", color = TextGray) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = TextGray,
                focusedTextColor = TextWhite,
                unfocusedTextColor = TextWhite,
                cursorColor = AccentPurple
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSendComment(text)
                    text = ""
                }
            },
            colors = IconButtonDefaults.iconButtonColors(containerColor = AccentPurple)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = AccentPurpleDark)
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = ChipBg,
            modifier = Modifier.size(40.dp)
        ) {
            if (comment.userPhotoUrl != null) {
                AsyncImage(model = comment.userPhotoUrl, contentDescription = null, contentScale = ContentScale.Crop)
            } else {
                Icon(Icons.Default.Person, contentDescription = null, tint = TextGray, modifier = Modifier.padding(8.dp))
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.userName, color = TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Just now", color = TextGray, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(comment.text, color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
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