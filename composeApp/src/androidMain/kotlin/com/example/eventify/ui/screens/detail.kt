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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.eventify.model.ChatMessage
import com.example.eventify.model.Comment
import com.example.eventify.model.Event
import com.example.eventify.model.Review
import com.example.eventify.ui.Screen
import kotlinx.datetime.LocalDateTime
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
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isFollowing by viewModel.isFollowingOrganizer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        containerColor = BgDark,
        bottomBar = {
            event?.let { currentEvent ->
                if (selectedTab != 4 || !viewModel.isRegistered) {
                    BottomActionSection(
                        isRegistered = viewModel.isRegistered,
                        onRsvpClick = {
                            if (viewModel.isRegistered) viewModel.toggleRsvp()
                            else navController.navigate(Screen.purchase(currentEvent.id))
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        if (isLoading || event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPurple)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(bottom = paddingValues.calculateBottomPadding())) {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
                        HeaderSection(
                            event = event!!,
                            onBackClick = { navController.popBackStack() },
                            onShareClick = { viewModel.registerShare() },
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }

                    item {
                        InfoSection(event = event!!, isFollowing = isFollowing, onFollowClick = { viewModel.toggleFollow() })
                        Spacer(modifier = Modifier.height(24.dp))
                        AttendeesPreviewSection(allAttendees = attendees)
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    item {
                        TabsSection(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    when (selectedTab) {
                        0 -> { // Detalhes
                            item {
                                Column {
                                    AboutSection(description = event!!.description)
                                    Spacer(modifier = Modifier.height(24.dp))
                                    // PONTO 1: Passar as coordenadas reais aqui
                                    LocationMapSection(
                                        locationName = event!!.locationName,
                                        lat = event!!.latitude,
                                        lon = event!!.longitude
                                    )
                                    Spacer(modifier = Modifier.height(24.dp))
                                    TagsSection(category = event!!.category, price = event!!.price)
                                    Spacer(modifier = Modifier.height(40.dp))
                                }
                            }
                        }
                        1 -> { // Participantes
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                                    if (attendees.isEmpty()) Text("Ninguém inscrito ainda.", color = TextGray)
                                    else Text("${attendees.size} pessoas vão a este evento!", color = TextWhite)
                                }
                            }
                        }
                        2 -> { // Comentários
                            item {
                                CommentInputSection(onSendComment = { text -> viewModel.sendComment(text) })
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            if (comments.isEmpty()) {
                                item { Text("Sem comentários ainda.", color = TextGray, modifier = Modifier.padding(horizontal = 20.dp)) }
                            } else {
                                items(comments) { comment ->
                                    CommentItem(comment)
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                        3 -> { // Avaliações
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
                            if (reviews.isEmpty()) item { Text("Sem avaliações ainda.", color = TextGray, modifier = Modifier.padding(horizontal = 20.dp)) }
                            else items(reviews) { review -> ReviewItem(review); Spacer(modifier = Modifier.height(16.dp)) }
                            item { Spacer(modifier = Modifier.height(40.dp)) }
                        }
                        4 -> { // Chat
                            if (viewModel.isRegistered) {
                                if (chatMessages.isEmpty()) {
                                    item {
                                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                            Text("O chat está vazio. Diz olá! 👋", color = TextGray)
                                        }
                                    }
                                } else {
                                    items(chatMessages) { msg ->
                                        ChatBubble(message = msg, isMe = msg.userId == userId)
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                                item {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    CommentInputSection(onSendComment = { viewModel.sendMessage(it) })
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            } else {
                                item { LockedChatState() }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- COMPONENTES ATUALIZADOS ---

@Composable
fun LocationMapSection(locationName: String, lat: Double, lon: Double) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Localização", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(4.dp))
        Text(locationName, style = MaterialTheme.typography.bodyMedium, color = TextGray)
        Spacer(modifier = Modifier.height(12.dp))

        // Cartão do Mapa com clique para abrir GPS real
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1E1E2C))
            .clickable {
                // Abre o Maps nas coordenadas exatas (PONTO 1)
                val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon($locationName)")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                intent.setPackage("com.google.android.apps.maps")
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // Fallback se não tiver Google Maps
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                }
            }
        ) {
            // Placeholder visual do mapa (Podes trocar pela API do Static Maps se tiveres Key)
            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF252535)))

            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Map, null, tint = AccentPurple, modifier = Modifier.size(40.dp))
                Spacer(Modifier.height(8.dp))
                Text("Abrir no Google Maps", color = TextWhite, fontWeight = FontWeight.Bold)
            }

            Text(
                "Tocar para navegar",
                color = AccentPurple.copy(0.7f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
            )
        }
    }
}

@Composable
fun TabsSection(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("Detalhes", "Quem vai", "Perguntas", "Avaliações", "Chat")

    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = BgDark,
        contentColor = AccentPurple,
        edgePadding = 16.dp,
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                color = AccentPurple
            )
        },
        divider = { HorizontalDivider(color = TextGray.copy(alpha = 0.2f)) }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = title,
                        color = if (selectedTab == index) TextWhite else TextGray,
                        fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
fun LockedChatState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Lock, null, tint = TextGray, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("Área Exclusiva", style = MaterialTheme.typography.titleMedium, color = TextWhite, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("Compra um bilhete para entrar no chat!", style = MaterialTheme.typography.bodyMedium, color = TextGray)
    }
}

@Composable
fun AboutSection(description: String) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Sobre este Evento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(12.dp))
        Text(description, style = MaterialTheme.typography.bodyMedium, color = TextGray, lineHeight = 24.sp)
    }
}

@OptIn(InternalSerializationApi::class)
@Composable
fun AttendeesPreviewSection(allAttendees: List<Attendee>) {
    if (allAttendees.isEmpty()) return
    val visibleAttendees = allAttendees.filter { it.isPublic && it.photoUrl.isNotBlank() }.take(5)
    val totalCount = allAttendees.size
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Quem vai", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (visibleAttendees.isNotEmpty()) {
                Box(modifier = Modifier.width((30 * visibleAttendees.size).dp + 10.dp)) {
                    visibleAttendees.forEachIndexed { index, attendee ->
                        AsyncImage(
                            model = attendee.photoUrl,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.padding(start = (25 * index).dp).size(40.dp).clip(CircleShape).border(2.dp, BgDark, CircleShape).zIndex(5f - index)
                        )
                    }
                }
            }
            val remainingText = if (totalCount > 1) "+ $totalCount pessoas inscritas" else "está inscrito"
            Text(remainingText, style = MaterialTheme.typography.bodyMedium, color = AccentPurple, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun BottomActionSection(isRegistered: Boolean, onRsvpClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().background(BgDark).padding(20.dp)) {
        Button(
            onClick = onRsvpClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = if (isRegistered) Color.Gray else AccentPurple, contentColor = if (isRegistered) TextWhite else AccentPurpleDark)
        ) {
            Icon(Icons.Default.ConfirmationNumber, null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(if (isRegistered) "CANCELAR RESERVA" else "RESERVAR AGORA", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}
// --- RESTANTES COMPONENTES TRADUZIDOS ---

@OptIn(ExperimentalSharedTransitionApi::class, InternalSerializationApi::class)
@Composable
fun HeaderSection(
    event: Event,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    val context = LocalContext.current
    with(sharedTransitionScope) {
        Box(modifier = Modifier.fillMaxWidth().height(350.dp)) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .sharedElement(
                        rememberSharedContentState(key = "image-${event.id}"),
                        animatedVisibilityScope
                    )
            )
            // Gradiente para o texto não sumir na imagem
            Box(modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(colors = listOf(Color.Transparent, BgDark), startY = 300f)
            ))

            // Botões de Topo
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 48.dp, start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.4f))
                ) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = TextWhite) }

                IconButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, "Dá uma olhadela neste evento: ${event.title}");
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Partilhar evento"))
                        onShareClick()
                    },
                    colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.4f))
                ) { Icon(Icons.Default.Share, "Partilhar", tint = TextWhite) }
            }

            Text(
                text = event.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextWhite,
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp)
            )
        }
    }
}

@OptIn(InternalSerializationApi::class)
@Composable
fun InfoSection(event: Event, isFollowing: Boolean, onFollowClick: () -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        // Data e Hora
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarMonth, null, tint = TextGray, modifier = Modifier.size(40.dp).background(ChipBg, CircleShape).padding(8.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(formatDateTime(event.dateTime), style = MaterialTheme.typography.bodyLarge, color = TextWhite)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Local (Nome)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = TextGray, modifier = Modifier.size(40.dp).background(ChipBg, CircleShape).padding(8.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(event.locationName, style = MaterialTheme.typography.bodyLarge, color = TextWhite)
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Organizador
        Row(
            modifier = Modifier.border(1.dp, TextGray.copy(0.3f), RoundedCornerShape(50)).padding(12.dp, 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(shape = CircleShape, color = Color(0xFF7B61FF), modifier = Modifier.size(24.dp)) {
                Box(contentAlignment = Alignment.Center) { Text("E", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White) }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text("Eventify Oficial", color = TextWhite, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.size(4.dp).background(TextGray, CircleShape))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isFollowing) "A Seguir" else "Seguir",
                color = if (isFollowing) TextGray else Color(0xFFD0BCFF),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable { onFollowClick() }
            )
        }
    }
}

@OptIn(InternalSerializationApi::class)
@Composable
fun ChatBubble(message: ChatMessage, isMe: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        if (!isMe) {
            Text(message.userName, style = MaterialTheme.typography.labelSmall, color = TextGray, modifier = Modifier.padding(start = 8.dp, bottom = 2.dp))
        }
        Surface(
            color = if (isMe) Color(0xFF7B61FF) else ChipBg,
            shape = RoundedCornerShape(
                topStart = 16.dp, topEnd = 16.dp,
                bottomStart = if (isMe) 16.dp else 4.dp,
                bottomEnd = if (isMe) 4.dp else 16.dp
            ),
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                color = TextWhite,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun CommentInputSection(onSendComment: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    Row(modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = { Text("Escreve um comentário...", color = TextGray) },
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPurple, unfocusedBorderColor = TextGray, focusedTextColor = TextWhite, unfocusedTextColor = TextWhite)
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = { if (text.isNotBlank()) { onSendComment(text); text = "" } },
            colors = IconButtonDefaults.iconButtonColors(containerColor = AccentPurple)
        ) { Icon(Icons.AutoMirrored.Filled.Send, "Enviar", tint = AccentPurpleDark) }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(horizontal = 20.dp)) {
        Surface(shape = CircleShape, color = ChipBg, modifier = Modifier.size(40.dp)) {
            if (comment.userPhotoUrl != null) AsyncImage(model = comment.userPhotoUrl, contentDescription = null, contentScale = ContentScale.Crop)
            else Icon(Icons.Default.Person, null, tint = TextGray, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.userName, color = TextWhite, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agora mesmo", color = TextGray, style = MaterialTheme.typography.labelSmall)
            }
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
            Text("Avalia este evento", style = MaterialTheme.typography.titleMedium, color = TextWhite, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                (1..5).forEach { index ->
                    Icon(
                        imageVector = if (index <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "$index Estrelas",
                        tint = StarYellow,
                        modifier = Modifier.size(32.dp).clickable { rating = index }
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Escreve a tua avaliação...", color = TextGray) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AccentPurple, unfocusedBorderColor = TextGray, focusedTextColor = TextWhite)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { if (rating > 0) { onSubmit(rating, text); text = ""; rating = 0 } },
                enabled = rating > 0,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) { Text("Publicar Avaliação", color = AccentPurpleDark, fontWeight = FontWeight.Bold) }
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
            Text("$count avaliações", color = TextGray, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(InternalSerializationApi::class)
@Composable
fun ReviewItem(review: Review) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.padding(horizontal = 20.dp)) {
        Surface(shape = CircleShape, color = ChipBg, modifier = Modifier.size(40.dp)) {
            if (review.userPhotoUrl != null) AsyncImage(model = review.userPhotoUrl, contentDescription = null, contentScale = ContentScale.Crop)
            else Icon(Icons.Default.Person, null, tint = TextGray, modifier = Modifier.padding(8.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(review.userName, color = TextWhite, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Row { repeat(review.rating) { Icon(Icons.Default.Star, null, tint = StarYellow, modifier = Modifier.size(14.dp)) } }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(review.comment, color = Color.LightGray, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun TagsSection(category: String, price: Double) {
    val tags = listOf(category, "Destaque", if (price == 0.0) "Grátis" else "Pago")
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text("Etiquetas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextWhite)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            tags.forEach { tag ->
                Box(modifier = Modifier.background(ChipBg, RoundedCornerShape(8.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(tag, color = TextGray, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

// Auxiliar de Formatação de Data
private fun formatDateTime(dateTime: String): String {
    return try {
        val parsed = LocalDateTime.parse(dateTime)
        val diaSemana = when(parsed.dayOfWeek.name) {
            "MONDAY" -> "Seg"
            "TUESDAY" -> "Ter"
            "WEDNESDAY" -> "Qua"
            "THURSDAY" -> "Qui"
            "FRIDAY" -> "Sex"
            "SATURDAY" -> "Sáb"
            "SUNDAY" -> "Dom"
            else -> parsed.dayOfWeek.name.take(3)
        }
        val mes = parsed.month.name.lowercase().replaceFirstChar { it.uppercase() }.take(3)
        val hora = parsed.hour.toString().padStart(2, '0')
        val minuto = parsed.minute.toString().padStart(2, '0')
        "$diaSemana, ${parsed.dayOfMonth} de $mes • $hora:$minuto"
    } catch (e: Exception) { dateTime }
}