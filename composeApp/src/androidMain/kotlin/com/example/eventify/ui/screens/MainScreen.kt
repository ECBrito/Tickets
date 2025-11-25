package com.example.eventify.ui.screens

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    // Estado para controlar qual tab está ativa
    var currentRoute by remember { mutableStateOf(BottomNavItem.Home.route) }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
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
                    // Grava o token na base de dados para receber notificações
                    scope.launch {
                        repository.updateUserFcmToken(currentUserId, task.result)
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B0A12),
        bottomBar = {
            EventifyBottomBar(
                onNavigate = { route -> currentRoute = route }
            )
        }
    ) { innerPadding ->

        // Animação de transição entre abas
        AnimatedContent(
            targetState = currentRoute,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            label = "MainTabs",
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
            }
        ) { targetRoute ->

            when (targetRoute) {
                // 1. HOME
                BottomNavItem.Home.route -> {
                    HomeScreenContent(
                        onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) },
                        onSeeAllClick = { currentRoute = BottomNavItem.Explore.route },
                        onNotificationsClick = { navController.navigate(Screen.NOTIFICATIONS) },
                        onProfileClick = { currentRoute = BottomNavItem.Profile.route },

                        // Scopes para Hero Animation
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope
                    )
                }

                // 2. EXPLORE
                BottomNavItem.Explore.route -> {
                    val viewModel = remember { AppModule.provideExploreViewModel() }
                    ExploreScreen(
                        viewModel = viewModel,
                        onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) },
                        onMapClick = { navController.navigate(Screen.EXPLORE_MAP) },
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope
                    )
                }

                // 3. MY EVENTS (Carteira + Favoritos)
                BottomNavItem.MyEvents.route -> {
                    MyEvents(
                        userId = currentUserId,
                        // Clique num Bilhete -> Abre QR Code
                        onTicketClick = { ticketId ->
                            navController.navigate(Screen.ticketDetail(ticketId, "My Ticket"))
                        },
                        // Clique num Favorito -> Abre Detalhes para comprar
                        onFavoriteClick = { eventId ->
                            navController.navigate(Screen.eventDetail(eventId))
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
                        onOrganizerClick = { navController.navigate(Screen.ORGANIZER_DASHBOARD) },
                        onEditProfileClick = { navController.navigate(Screen.EDIT_PROFILE) },
                        onInterestsClick = { navController.navigate(Screen.INTERESTS) }
                    )
                }

                else -> {
                    // Fallback seguro
                    Box(Modifier.fillMaxSize())
                }
            }
        }
    }
}