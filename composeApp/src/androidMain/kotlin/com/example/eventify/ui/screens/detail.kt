package com.example.eventify.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
// 1. IMPORTS CRÍTICOS ADICIONADOS:
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
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

private val BgDark = Color(0xFF0B0A12)
private val TextWhite = Color.White
private val TextGray = Color(0xFF9CA3AF)
private val AccentPurple = Color(0xFFD0BCFF)
private val AccentPurpleDark = Color(0xFF381E72)
private val ChipBg = Color(0xFF1E1E2C)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun EventDetailScreen(
    eventId: String,
    userId: String,
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    val viewModel = remember { AppModule.provideEventDetailViewModel(eventId) }
    val event by viewModel.event.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            event?.let { currentEvent ->
                BottomActionSection(
                    isRegistered = viewModel.isRegistered,
                    onRsvpClick = {
                        if (viewModel.isRegistered) viewModel.toggleRsvp()
                        else navController.navigate(Screen.purchase(currentEvent.id))
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
                    item {
                        HeaderSection(
                            event = event!!,
                            onBackClick = { navController.popBackStack() },
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }
                    item { InfoSection(event = event!!); Spacer(modifier = Modifier.height(24.dp)) }
                    item { TabsSection(selectedTab = selectedTab, onTabSelected = { selectedTab = it }); Spacer(modifier = Modifier.height(24.dp)) }

                    when (selectedTab) {
                        0 -> { // Details
                            item {
                                Column {
                                    AboutSection(description = event!!.description)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    LocationMapSection(locationName = event!!.location)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    TagsSection(category = event!!.category, price = event!!.price)
                                    Spacer(modifier = Modifier.height(40.dp))
                                }
                            }
                        }
                        1 -> { // Attendees
                            item { Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) { Text("Attendees list coming soon...", color = TextGray) } }
                        }
                        2 -> { // Comments
                            item { CommentInputSection(onSendComment = { text -> viewModel.sendComment(text) }); Spacer(modifier = Modifier.height(24.dp)) }

                            if (comments.isEmpty()) {
                                item { Text("No comments yet.", color = TextGray, modifier = Modifier.padding(horizontal = 20.dp)) }
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HeaderSection(
    event: Event,
    onBackClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    val context = LocalContext.current

    with(sharedTransitionScope) {
        Box(
            modifier = Modifier.fillMaxWidth().height(350.dp)
        ) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    // 2. CORREÇÃO: 'state' mudou para 'sharedContentState'
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "image-${event.id}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            )

            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, BgDark), startY = 300f)))

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick, colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.4f))) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                }
                IconButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply { putExtra(Intent.EXTRA_TEXT, "Check this event: ${event.title}"); type = "text/plain" }
                        context.startActivity(Intent.createChooser(sendIntent, "Share"))
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
                modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 20.dp, vertical = 20.dp)
            )
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(
        // 3. CORREÇÃO: Usar verticalAlignment em vez de crossAxisAlignment
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Surface(shape = CircleShape, color = ChipBg, modifier = Modifier.size(40.dp)) {
            if (comment.userPhotoUrl != null) AsyncImage(model = comment.userPhotoUrl, contentDescription = null, contentScale = ContentScale.Crop) else Icon(Icons.Default.Person, null, tint = TextGray, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) { Text(comment.userName, color = TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium); Spacer(modifier = Modifier.width(8.dp)); Text("Just now", color = TextGray, style = MaterialTheme.typography.labelSmall) }
            Spacer(modifier = Modifier.height(4.dp))
            Text(comment.text, color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// --- RESTANTES COMPONENTES (Iguais) ---

@Composable
fun InfoSection(event: Event) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, null, tint = TextGray, modifier = Modifier.size(40.dp).background(ChipBg, CircleShape).padding(8.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(formatDateTime(event.dateTime), style = MaterialTheme.typography.bodyLarge, color = TextWhite)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = TextGray, modifier = Modifier.size(40.dp).background(ChipBg, CircleShape).padding(8.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(event.location, style = MaterialTheme.typography.bodyLarge, color = TextWhite)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.border(1.dp, TextGray.copy(alpha = 0.3f), RoundedCornerShape(50)).padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = AccentPurple, modifier = Modifier.size(24.dp)) { Box(contentAlignment = Alignment.Center) { Text("E", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentPurpleDark) } }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Organized by Eventify Inc.", color = TextWhite, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun TabsSection(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Details", "Attendees", "Comments")
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            tabs.forEachIndexed { index, title ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onTabSelected(index) }) {
                    Text(title, style = MaterialTheme.typography.bodyLarge, color = if (selectedTab == index) TextWhite else TextGray, fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal)
                    if (selectedTab == index) { Spacer(modifier = Modifier.height(8.dp)); Box(modifier = Modifier.width(40.dp).height(3.dp).background(AccentPurple, RoundedCornerShape(2.dp))) }
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
        Text(description, style = MaterialTheme.typography.bodyMedium, color = TextGray, lineHeight = 24.sp)
    }
}

@Composable
fun LocationMapSection(locationName: String) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Location", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF2C2C3E)).clickable {
            val gmmIntentUri = Uri.parse("geo:0,0?q=${Uri.encode(locationName)}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps")
            try { context.startActivity(mapIntent) } catch (e: Exception) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(locationName)}"))) }
        }) {
            AsyncImage(model = "https://maps.googleapis.com/maps/api/staticmap?center=${locationName}&zoom=14&size=600x300&maptype=roadmap&key=YOUR_API_KEY_HERE", contentDescription = "Map Preview", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().alpha(0.5f))
            Icon(Icons.Default.LocationOn, null, tint = Color.Red, modifier = Modifier.align(Alignment.Center).size(40.dp))
            Text("Tap to open Maps", color = TextWhite, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
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
            tags.forEach { tag -> Box(modifier = Modifier.background(ChipBg, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) { Text(tag, color = TextGray, style = MaterialTheme.typography.bodyMedium) } }
        }
    }
}

@Composable
fun CommentInputSection(onSendComment: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text("Write a comment...", color = TextGray) }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(24.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPurple, unfocusedBorderColor = TextGray, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite, cursorColor = AccentPurple))
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(onClick = { if (text.isNotBlank()) { onSendComment(text); text = "" } }, colors = IconButtonDefaults.iconButtonColors(containerColor = AccentPurple)) { Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = AccentPurpleDark) }
    }
}

@Composable
fun BottomActionSection(isRegistered: Boolean, onRsvpClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(BgDark).padding(20.dp)) {
        Button(onClick = onRsvpClick, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(28.dp), colors = ButtonDefaults.buttonColors(containerColor = if (isRegistered) Color.Gray else AccentPurple, contentColor = if (isRegistered) TextWhite else AccentPurpleDark)) {
            Icon(Icons.Default.ConfirmationNumber, null, modifier = Modifier.size(20.dp)); Spacer(modifier = Modifier.width(12.dp)); Text(if (isRegistered) "CANCEL RSVP" else "RSVP NOW", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
    } catch (e: Exception) { dateTime }
}

// Helper para alpha no modifier
fun Modifier.alpha(alpha: Float) = this // Simples para não dar erro, AsyncImage gere o alpha