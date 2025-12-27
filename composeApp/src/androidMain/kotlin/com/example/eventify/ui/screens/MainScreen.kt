package com.example.eventify.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.eventify.di.AppModule
import com.example.eventify.ui.Screen
import com.example.eventify.ui.components.BottomNavItem
import com.example.eventify.ui.components.EventifyBottomBar
import com.example.eventify.ui.utils.getCurrentLocation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun MainScreen(
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    // 1. Definições Iniciais
    var currentRoute by remember { mutableStateOf(BottomNavItem.Home.route) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val repository = remember { AppModule.eventRepository }

    // 2. VIEWMODEL (Criar primeiro para poder usar as propriedades abaixo)
    val homeViewModel = remember { AppModule.provideHomeViewModel() }

    // 3. COLETAR ESTADOS (userLat e userLon agora existem para o compilador)
    val userLat by homeViewModel.userLat.collectAsState(initial = null)
    val userLon by homeViewModel.userLon.collectAsState(initial = null)

    // 4. Lançador de Permissão de GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            getCurrentLocation(context) { lat, lon ->
                homeViewModel.updateUserLocation(lat, lon)
            }
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        )
    }

    // 5. Permissão de Notificações (Android 13+)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val notificationPermission = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
        LaunchedEffect(Unit) {
            delay(1500)
            if (!notificationPermission.status.isGranted) {
                notificationPermission.launchPermissionRequest()
            }
        }
    }

    // 6. Token FCM
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    scope.launch { repository.updateUserFcmToken(currentUserId, task.result) }
                }
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B0A12),
        bottomBar = { EventifyBottomBar(onNavigate = { route -> currentRoute = route }) }
    ) { innerPadding ->

        AnimatedContent(
            targetState = currentRoute,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            label = "MainTabs",
            transitionSpec = { fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300)) }
        ) { targetRoute ->

            when (targetRoute) {
                BottomNavItem.Home.route -> {
                    HomeScreenContent(
                        viewModel = homeViewModel,
                        userLat = userLat,
                        userLon = userLon,
                        onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) },
                        onSeeAllClick = { currentRoute = BottomNavItem.Explore.route },
                        onSearchClick = { /* Opcional: Navegar para pesquisa */ },
                        onNotificationsClick = { navController.navigate(Screen.NOTIFICATIONS) },
                        onProfileClick = { currentRoute = BottomNavItem.Profile.route },
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope
                    )
                }

                BottomNavItem.Explore.route -> {
                    ExploreScreen(
                        onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) },
                        onMapClick = { navController.navigate(Screen.EXPLORE_MAP) },
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope
                    )
                }

                BottomNavItem.MyEvents.route -> {
                    MyEvents(
                        userId = currentUserId,
                        onTicketClick = { tid -> navController.navigate(Screen.ticketDetail(tid, "O Meu Bilhete")) },
                        onFavoriteClick = { eid -> navController.navigate(Screen.eventDetail(eid)) }
                    )
                }

                BottomNavItem.Profile.route -> {
                    ProfileScreen(
                        onLogoutClick = {
                            FirebaseAuth.getInstance().signOut()
                            navController.navigate(Screen.AUTH_ROOT) { popUpTo(0) { inclusive = true } }
                        },
                        onOrganizerClick = { navController.navigate(Screen.ORGANIZER_DASHBOARD) },
                        onEditProfileClick = { navController.navigate(Screen.EDIT_PROFILE) },
                        onInterestsClick = { navController.navigate(Screen.INTERESTS) }
                    )
                }
                else -> Box(Modifier.fillMaxSize())
            }
        }
    }
}