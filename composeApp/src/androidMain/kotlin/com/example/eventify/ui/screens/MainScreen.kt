package com.example.eventify.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.example.eventify.di.AppModule
import com.example.eventify.ui.Screen
import com.example.eventify.ui.components.BottomNavItem // O teu componente
import com.example.eventify.ui.components.EventifyBottomBar // O teu componente
import com.example.eventify.ui.screens.organizer.OrganizerDashboardScreen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun MainScreen(
    navController: NavController
) {
    // Estado para controlar a tab ativa usando as ROTAS do teu componente
    var currentRoute by remember { mutableStateOf(BottomNavItem.Home.route) }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Scaffold(
        containerColor = Color(0xFF0B0A12),
        bottomBar = {
            // Usando a TUA barra personalizada de AppBars.kt
            EventifyBottomBar(
                onNavigate = { route -> currentRoute = route }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // Trocamos o ecrã com base na Rota (String) definida em AppBars.kt
            when (currentRoute) {
                BottomNavItem.Home.route -> HomeScreenContent(
                    onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) },
                    onSeeAllClick = { currentRoute = BottomNavItem.Explore.route } // Vai para Explore
                )
                // Dentro do when(currentRoute)
                BottomNavItem.Explore.route -> {
                    // O ViewModel é injetado DENTRO do ExploreScreen agora,
                    // mas se quiseres podes manter o remember aqui.
                    // A minha versão do ExploreScreen acima já tem o remember lá dentro.

                    ExploreScreen(
                        onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) },
                        onMapClick = { navController.navigate(Screen.EXPLORE_MAP) } // <--- Passa a navegação para o mapa
                    )
                }
                BottomNavItem.MyEvents.route -> MyEvents(
                    userId = currentUserId,
                    onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) }
                )
                BottomNavItem.Profile.route -> ProfileScreen(
                    onLogoutClick = {
                        // 1. LOGOUT DO FIREBASE (O passo que faltava!)
                        try {
                            FirebaseAuth.getInstance().signOut()
                        } catch (e: Exception) {
                            println("Erro ao fazer logout: ${e.message}")
                        }

                        // 2. NAVEGAÇÃO SEGURA
                        // Volta para o ecrã de Login (AUTH_ROOT) e limpa a pilha de trás
                        // para que o utilizador não possa voltar ao Perfil clicando em "Voltar"
                        navController.navigate(Screen.AUTH_ROOT) {
                            popUpTo(0) { inclusive = true } // Limpa tudo
                            launchSingleTop = true
                        }
                    },
                    onOrganizerClick = {
                        navController.navigate(Screen.ORGANIZER_DASHBOARD)
                    }
                )

            }
        }
    }
}