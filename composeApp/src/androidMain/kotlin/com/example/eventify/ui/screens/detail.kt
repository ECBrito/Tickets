package com.example.eventify.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
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
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.Attendee
import com.example.eventify.model.Comment
import com.example.eventify.model.Event
import com.example.eventify.model.Review
import com.example.eventify.ui.Screen
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.serialization.InternalSerializationApi

// Cores
private val BgDark = Color(0xFF0B0A12)
private val TextWhite = Color.White
private val TextGray = Color(0xFF9CA3AF)
private val AccentPurple = Color(0xFFD0BCFF)
private val AccentPurpleDark = Color(0xFF381E72)
private val ChipBg = Color(0xFF1E1E2C)
private val StarYellow = Color(0xFFFFD700)

@OptIn(ExperimentalSharedTransitionApi::class, InternalSerializationApi::class)
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
    val attendees by viewModel.attendees.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    val isFollowing by viewModel.isFollowingOrganizer.collectAsState() // Estado de Follow
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
                LazyColumn(modifier = Modifier.weight(1f)) {
                    // 1. Header
                    item {
                        HeaderSection(
                            event = event!!,
                            onBackClick = { navController.popBackStack() },
                            onShareClick = { viewModel.registerShare() },
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }

                    // 2. Info + Attendees
                    item {
                        // Passamos o estado isFollowing e a ação toggleFollow
                        InfoSection(
                            event = event!!,
                            isFollowing = isFollowing,
                            onFollowClick = { viewModel.toggleFollow() }
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        AttendeesPreviewSection(allAttendees = attendees)

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // 3. Tabs
                    item {
                        TabsSection(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // 4. Conteúdo
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
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                    if (attendees.isEmpty()) Text("No one yet.", color = TextGray)
                                    else Text("${attendees.size} people going!", color = TextWhite)
                                }
                            }
                        }
                        2 -> { // Comments
                            item {
                                CommentInputSection(onSendComment = { text -> viewModel.sendComment(text) })
                                Spacer(modifier = Modifier.height(24.dp))
                            }
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
                        3 -> { // Reviews
                            if (viewModel.isRegistered) {
                                item {
                                    ReviewInputSection(onSubmit = { rating, text -> viewModel.submitReview(rating, text) })
                                    Spacer(modifier = Modifier.height(24.dp))
                                }
                            }
                            item {
                                RatingSummary(rating = event!!.rating, count = event!!.reviewCount)
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                            if (reviews.isEmpty()) item { Text("No reviews yet.", color = TextGray, modifier = Modifier.padding(horizontal = 20.dp)) }
                            else items(reviews) { review -> ReviewItem(review); Spacer(modifier = Modifier.height(16.dp)) }
                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTES ---

@OptIn(InternalSerializationApi::class)
@Composable
fun InfoSection(
    event: Event,
    isFollowing: Boolean, // <--- Parâmetro Novo
    onFollowClick: () -> Unit // <--- Parâmetro Novo
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {

        // Data
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, null, tint = TextGray, modifier = Modifier.size(40.dp).background(ChipBg, CircleShape).padding(8.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(formatDateTime(event.dateTime), style = MaterialTheme.typography.bodyLarge, color = TextWhite)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Local
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = TextGray, modifier = Modifier.size(40.dp).background(ChipBg, CircleShape).padding(8.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(event.location, style = MaterialTheme.typography.bodyLarge, color = TextWhite)
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Organizador + Follow
        Row(
            modifier = Modifier
                .border(1.dp, TextGray.copy(alpha = 0.3f), RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = AccentPurple, modifier = Modifier.size(24.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text("E", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentPurpleDark)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Eventify Inc.", color = TextWhite, style = MaterialTheme.typography.bodyMedium)

            // Separador e Botão Follow
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(4.dp).background(TextGray, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (isFollowing) "Following" else "Follow",
                color = if (isFollowing) TextGray else AccentPurple,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onFollowClick() }
            )
        }
    }
}

// --- RESTANTES COMPONENTES (Mantidos iguais para não quebrar) ---

@OptIn(InternalSerializationApi::class)
@Composable
fun AttendeesPreviewSection(allAttendees: List<Attendee>) {
    if (allAttendees.isEmpty()) return
    val visibleAttendees = allAttendees.filter { it.isPublic && it.photoUrl.isNotBlank() }.take(5)
    val totalCount = allAttendees.size
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Who's Going", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (visibleAttendees.isNotEmpty()) {
                Box(modifier = Modifier.width((30 * visibleAttendees.size).dp + 40.dp)) {
                    visibleAttendees.forEachIndexed { index, attendee ->
                        AsyncImage(model = attendee.photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.padding(start = (28 * index).dp).size(40.dp).clip(CircleShape).border(2.dp, BgDark, CircleShape).zIndex(5f - index))
                    }
                }
            } else {
                Icon(Icons.Default.Person, null, tint = TextGray, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(8.dp))
            }
            val remainingText = if (totalCount > 1) "+ $totalCount others going" else "is going"
            Text(remainingText, style = MaterialTheme.typography.bodyMedium, color = AccentPurple, fontWeight = FontWeight.SemiBold)
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, InternalSerializationApi::class)
@Composable
fun HeaderSection(event: Event, onBackClick: () -> Unit, onShareClick: () -> Unit, animatedVisibilityScope: AnimatedVisibilityScope, sharedTransitionScope: SharedTransitionScope) {
    val context = LocalContext.current
    with(sharedTransitionScope) {
        Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
            AsyncImage(model = event.imageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().sharedElement(rememberSharedContentState(key = "image-${event.id}"), animatedVisibilityScope))
            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colors = listOf(Color.Transparent, BgDark), startY = 300f)))
            Row(modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp, end = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                IconButton(onClick = onBackClick, colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.4f))) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextWhite) }
                IconButton(onClick = { val sendIntent = Intent(Intent.ACTION_SEND).apply { putExtra(Intent.EXTRA_TEXT, "Check this: ${event.title}"); type = "text/plain" }; context.startActivity(Intent.createChooser(sendIntent, "Share")); onShareClick() }, colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.4f))) { Icon(Icons.Default.Share, "Share", tint = TextWhite) }
            }
            Text(event.title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = TextWhite, modifier = Modifier.align(Alignment.BottomStart).padding(20.dp))
        }
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
            try { context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri).setPackage("com.google.android.apps.maps")) }
            catch (e: Exception) { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${Uri.encode(locationName)}"))) }
        }) {
            AsyncImage(model = "https://maps.googleapis.com/maps/api/staticmap?center=${locationName}&zoom=14&size=600x300&maptype=roadmap&key=YOUR_KEY", contentDescription = "Map", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize().alpha(0.5f))
            Icon(Icons.Default.LocationOn, null, tint = Color.Red, modifier = Modifier.align(Alignment.Center).size(40.dp))
            Text("Tap to open Maps", color = TextWhite, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
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
fun CommentItem(comment: Comment) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(horizontal = 20.dp)) {
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

@Composable
fun ReviewInputSection(onSubmit: (Int, String) -> Unit) {
    var rating by remember { mutableIntStateOf(0) }
    var text by remember { mutableStateOf("") }
    Card(colors = CardDefaults.cardColors(containerColor = ChipBg), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Rate this event", style = MaterialTheme.typography.titleMedium, color = TextWhite, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row { (1..5).forEach { index -> Icon(imageVector = if (index <= rating) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = "$index Stars", tint = StarYellow, modifier = Modifier.size(32.dp).clickable { rating = index }) } }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = text, onValueChange = { text = it }, placeholder = { Text("Write your review...", color = TextGray) }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPurple, unfocusedBorderColor = TextGray, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite))
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { if (rating > 0) { onSubmit(rating, text); text = ""; rating = 0 } }, enabled = rating > 0, modifier = Modifier.align(Alignment.End), colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)) { Text("Post Review") }
        }
    }
}

@Composable
fun RatingSummary(rating: Double, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(text = "%.1f".format(rating), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row { repeat(5) { index -> Icon(imageVector = if (index < rating.toInt()) Icons.Default.Star else Icons.Default.StarBorder, contentDescription = null, tint = StarYellow, modifier = Modifier.size(16.dp)) } }
            Text("$count reviews", color = TextGray, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(InternalSerializationApi::class)
@Composable
fun ReviewItem(review: Review) {
    Row(
        // CORREÇÃO: Usar verticalAlignment em vez de crossAxisAlignment
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = ChipBg,
            modifier = Modifier.size(40.dp)
        ) {
            if (review.userPhotoUrl != null) {
                AsyncImage(
                    model = review.userPhotoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    null,
                    tint = TextGray,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(review.userName, color = TextWhite, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Row {
                    repeat(review.rating) {
                        Icon(Icons.Default.Star, null, tint = StarYellow, modifier = Modifier.size(14.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(review.comment, color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun TabsSection(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Details", "Attendees", "Comments", "Reviews")
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
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

fun Modifier.alpha(alpha: Float) = this