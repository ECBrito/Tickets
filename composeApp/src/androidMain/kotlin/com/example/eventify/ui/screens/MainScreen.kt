package com.example.eventify.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.eventify.di.AppModule
import com.example.eventify.ui.Screen
import com.example.eventify.ui.components.BottomNavItem
import com.example.eventify.ui.components.EventifyBottomBar
import com.example.eventify.ui.screens.organizer.OrganizerDashboardScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainScreen(
    navController: NavController
) {
    // Estado para controlar qual tab está ativa
    var currentRoute by remember { mutableStateOf(BottomNavItem.Home.route) }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Scaffold(
        containerColor = Color(0xFF0B0A12),
        bottomBar = {
            // Barra de navegação personalizada
            EventifyBottomBar(
                onNavigate = { route -> currentRoute = route }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {

            // O 'when' decide que ecrã mostrar com base na rota atual
            when (currentRoute) {
                // 1. HOME
                BottomNavItem.Home.route -> {
                    HomeScreenContent(
                        onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) },
                        onSeeAllClick = { currentRoute = BottomNavItem.Explore.route }
                    )
                }

                // 2. EXPLORE
                BottomNavItem.Explore.route -> {
                    val viewModel = remember { AppModule.provideExploreViewModel() }
                    ExploreScreen(
                        viewModel = viewModel,
                        onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) },
                        onMapClick = { navController.navigate(Screen.EXPLORE_MAP) }
                    )
                }

                // 3. MY EVENTS
                BottomNavItem.MyEvents.route -> {
                    MyEvents(
                        userId = currentUserId,
                        onEventClick = { ticketId ->
                            // Clicar aqui abre o QR Code do bilhete
                            navController.navigate(Screen.ticketDetail(ticketId, "My Ticket"))
                        }
                    )
                }

                // 4. PROFILE
                BottomNavItem.Profile.route -> {
                    ProfileScreen(
                        onLogoutClick = {
                            try {
                                FirebaseAuth.getInstance().signOut()
                            } catch (e: Exception) { }

                            navController.navigate(Screen.AUTH_ROOT) {
                                popUpTo(0) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                        onOrganizerClick = {
                            navController.navigate(Screen.ORGANIZER_DASHBOARD)
                        },
                        onEditProfileClick = {
                            navController.navigate(Screen.EDIT_PROFILE)
                        }
                    )
                }

                // 5. ELSE (Obrigatório em Kotlin quando a variável é String)
                else -> {
                    // Fallback: mostrar Home ou nada
                    HomeScreenContent(
                        onEventClick = { navController.navigate(Screen.eventDetail(it)) },
                        onSeeAllClick = { currentRoute = BottomNavItem.Explore.route }
                    )
                }
            }
        }
    }
}