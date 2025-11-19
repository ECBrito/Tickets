package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.eventify.model.Event
import com.example.eventify.ui.Screen
import com.example.eventify.ui.components.EventCard
import com.example.eventify.ui.components.EventifyBottomBar
import com.example.eventify.ui.components.IconButtonWithBadge
import com.example.eventify.ui.theme.EventifyTheme
import com.example.eventify.ui.viewmodels.HomeViewModel

// =====================================================================
// 1. WRAPPER DE NAVEGAÇÃO (Controla as abas e injeta o ViewModel)
// =====================================================================

@Composable
fun HomeScreenWrapper(
    navController: NavHostController,
    homeViewModel: HomeViewModel = viewModel() // Injeta/Cria o ViewModel
) {
    val bottomNavController = rememberNavController()

    // Coletar os estados do ViewModel
    val featuredEvents by homeViewModel.featuredEvents.collectAsState()
    val upcomingEvents by homeViewModel.upcomingEvents.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()

    Scaffold(
        bottomBar = {
            EventifyBottomBar(
                onNavigate = { route ->
                    bottomNavController.navigate(route) {
                        // Configuração padrão para navegação em abas:
                        // 1. Pop até o início para evitar empilhamento infinito
                        popUpTo(bottomNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // 2. Evita múltiplas cópias da mesma tela
                        launchSingleTop = true
                        // 3. Restaura o estado ao voltar para a aba
                        restoreState = true
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        // --- Navegação das Abas ---
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Aba 1: Home (Feed)
            composable("home") {
                // CORREÇÃO: Recarregar dados sempre que esta tela for exibida
                // Isto garante que novos eventos criados apareçam logo
                LaunchedEffect(Unit) {
                    homeViewModel.loadData()
                }

                // Wrapper interno para incluir a TopBar específica da Home
                Scaffold(
                    topBar = {
                        HomeTopBar(
                            onSearchClick = { bottomNavController.navigate("explore") },
                            onNotificationsClick = { /* TODO */ },
                            onProfileClick = { bottomNavController.navigate("profile") }
                        )
                    }
                ) { homePadding ->
                    HomeScreenContent(
                        modifier = Modifier.padding(homePadding),
                        featuredEvents = featuredEvents,
                        upcomingEvents = upcomingEvents,
                        isLoading = isLoading,
                        onEventClick = { eventId ->
                            // Navega para o Detalhe usando o controlador RAIZ (navController)
                            navController.navigate(Screen.eventDetail(eventId))
                        },
                        onSaveClick = { eventId ->
                            homeViewModel.toggleSave(eventId)
                        }
                    )
                }
            }

            // Aba 2: Explore
            composable("explore") {
                ExploreScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Screen.eventDetail(eventId))
                    }
                )
            }

            // Aba 3: My Events
            composable("myevents") {
                MyEventsScreen(
                    onEventClick = { eventId ->
                        navController.navigate(Screen.eventDetail(eventId))
                    }
                )
            }

            // Aba 4: Profile
            composable("profile") {
                ProfileScreen(
                    onLogoutClick = {
                        // Sai para o Login e limpa a pilha
                        navController.navigate(Screen.AUTH_ROOT) {
                            popUpTo(Screen.HOME_ROOT) { inclusive = true }
                        }
                    },
                    onOrganizerClick = {
                        // Navega para o Dashboard do Organizador (rota raiz)
                        navController.navigate(Screen.ORGANIZER_DASHBOARD)
                    }
                )
            }
        }
    }
}

// =====================================================================
// 2. COMPONENTES DA UI DA HOME
// =====================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onSearchClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    TopAppBar(
        title = { Text("Eventify") },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface // Cor de superfície escura
        ),
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, contentDescription = "Search events")
            }
            IconButtonWithBadge(
                icon = Icons.Default.Notifications,
                badgeCount = 3,
                onClick = onNotificationsClick
            )
            IconButton(onClick = onProfileClick) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
            }
        }
    )
}

// Conteúdo principal da Home (Feed de Eventos)
@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    featuredEvents: List<Event>,
    upcomingEvents: List<Event>,
    isLoading: Boolean,
    onEventClick: (String) -> Unit,
    onSaveClick: (String) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- Seção Featured ---
        item {
            SectionHeader("Featured This Week")
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                // Skeleton para o Carrossel
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(2) {
                        FeatureCardPlaceholder()
                    }
                }
            } else {
                FeaturedEventsCarousel(
                    events = featuredEvents,
                    onEventClick = onEventClick
                )
            }
        }

        // --- Seção Upcoming ---
        item {
            SectionHeader("Upcoming Near You")
        }

        if (isLoading) {
            // Skeleton para a Lista Vertical
            items(3) {
                EventCardPlaceholder()
            }
        } else {
            items(upcomingEvents) { event ->
                EventCard(
                    event = event,
                    onClick = onEventClick,
                    onSave = onSaveClick
                )
            }

            if (upcomingEvents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No upcoming events found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// --- Helpers e Componentes Visuais ---

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun FeaturedEventsCarousel(
    events: List<Event>,
    onEventClick: (String) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(events) { event ->
            FeatureCard(event = event, onClick = { onEventClick(event.id) })
        }
    }
}

@Composable
fun FeatureCard(
    event: Event,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(280.dp), // Largura ligeiramente maior para destaque
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = "Poster for ${event.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp) // Altura fixa para consistência
                    .clip(MaterialTheme.shapes.medium)
            )
            Column(Modifier.padding(16.dp)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                // Formatação manual simples para data
                val dateStr = "${event.dateTime.dayOfWeek.name.take(3)}, ${event.dateTime.dayOfMonth} ${event.dateTime.month.name.take(3)}"
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Placeholder para o Carrossel
@Composable
fun FeatureCardPlaceholder() {
    Card(
        modifier = Modifier.width(280.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
            Column(Modifier.padding(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(24.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
            }
        }
    }
}

// Placeholder para a Lista Vertical
@Composable
fun EventCardPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
                Box(modifier = Modifier.fillMaxWidth(0.8f).height(20.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
                Box(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
            }
        }
    }
}

@Preview
@Composable
fun HomePreview() {
    EventifyTheme(darkTheme = true) {
        // Preview sem ViewModel real
        HomeScreenWrapper(navController = rememberNavController())
    }
}