package com.example.eventify.ui.screens

import android.Manifest
import android.os.Build
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
import com.example.eventify.ui.components.BottomNavItem
import com.example.eventify.ui.components.EventifyBottomBar
import com.example.eventify.ui.screens.organizer.OrganizerDashboardScreen
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class) // Necessário para a gestão de permissões
@Composable
fun MainScreen(
    navController: NavController
) {
    // Estado para controlar qual tab está ativa
    var currentRoute by remember { mutableStateOf(BottomNavItem.Home.route) }
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Injeção do repositório e scope para operações assíncronas
    val repository = remember { AppModule.eventRepository }
    val scope = rememberCoroutineScope()

    // --- 1. PEDIR PERMISSÃO DE NOTIFICAÇÕES (Android 13+) ---
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)

        LaunchedEffect(Unit) {
            if (!notificationPermission.status.isGranted) {
                notificationPermission.launchPermissionRequest()
            }
        }
    }

    // --- 2. OBTER E GUARDAR O TOKEN FCM ---
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val token = task.result
                    println("FCM Token Obtido: $token")

                    // Grava o token na base de dados do utilizador
                    scope.launch {
                        repository.updateUserFcmToken(currentUserId, token)
                    }
                } else {
                    println("Falha ao obter token FCM: ${task.exception}")
                }
            }
        }
    }

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