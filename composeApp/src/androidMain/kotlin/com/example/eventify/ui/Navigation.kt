package com.example.eventify.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.eventify.ui.screens.EventDetailScreen
import com.example.eventify.ui.screens.HomeScreenWrapper
import com.example.eventify.ui.screens.NotificationsScreen // NOVO IMPORT
import com.example.eventify.ui.screens.OnboardingScreen
import com.example.eventify.ui.screens.auth.ForgotPasswordScreen
import com.example.eventify.ui.screens.auth.SignInScreen
import com.example.eventify.ui.screens.auth.SignUpScreen
import com.example.eventify.ui.screens.organizer.CreateEventScreen
import com.example.eventify.ui.screens.organizer.OrganizerDashboardScreen
import com.example.eventify.ui.screens.ExploreMapScreen // NOVO IMPORT

// Objeto de Rotas (para usar em toda a app)
object Screen {
    const val ONBOARDING = "onboarding"
    const val AUTH_ROOT = "auth_root"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"

    const val HOME_ROOT = "home_root"
    const val EVENT_DETAIL = "event/{eventId}"

    const val ORGANIZER_DASHBOARD = "organizer_dashboard"
    const val CREATE_EVENT = "create_event"

    const val NOTIFICATIONS = "notifications" // NOVA ROTA
    const val EXPLORE_MAP = "explore_map"     // NOVA ROTA

    fun eventDetail(eventId: String): String = "event/$eventId"
}

@Composable
fun EventifyNavHost(
    navController: NavHostController,
    startDestination: String = Screen.ONBOARDING
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ==========================================
        // 0. Onboarding
        // ==========================================
        composable(Screen.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    navController.navigate(Screen.AUTH_ROOT) {
                        popUpTo(Screen.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // ... (Fluxos Auth mantêm-se iguais) ...
        composable(Screen.AUTH_ROOT) {
            SignInScreen(
                onSignInClick = { navController.navigate(Screen.HOME_ROOT) { popUpTo(Screen.AUTH_ROOT) { inclusive = true } } },
                onForgotPasswordClick = { navController.navigate(Screen.FORGOT_PASSWORD) },
                onSignUpClick = { navController.navigate(Screen.SIGN_UP) }
            )
        }
        composable(Screen.SIGN_UP) {
            SignUpScreen(
                onSignUpClick = { navController.navigate(Screen.HOME_ROOT) { popUpTo(Screen.AUTH_ROOT) { inclusive = true } } },
                onSignInClick = { navController.popBackStack() }
            )
        }
        composable(Screen.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onSendLinkClick = { navController.popBackStack() }
            )
        }

        // ==========================================
        // 2. Fluxo Principal (Utilizador)
        // ==========================================

        // Home Wrapper (Contém as abas)
        composable(Screen.HOME_ROOT) {
            HomeScreenWrapper(navController = navController)
        }

        // Detalhes do Evento
        composable(
            route = Screen.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(
                eventId = eventId,
                onBackClick = { navController.popBackStack() },
                onShareClick = { /* Implementar share */ }
            )
        }

        // --- NOVA TELA: Notificações ---
        composable(Screen.NOTIFICATIONS) {
            NotificationsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // ==========================================
        // 3. Fluxo do Organizador
        // ==========================================

        composable(Screen.ORGANIZER_DASHBOARD) {
            OrganizerDashboardScreen(
                onCreateEventClick = { navController.navigate(Screen.CREATE_EVENT) },
                onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) }
            )
        }

        composable(Screen.CREATE_EVENT) {
            CreateEventScreen(
                onBackClick = { navController.popBackStack() },
                onPublishClick = { navController.popBackStack() }
            )
        }

        // --- NOVA TELA: Mapa de Exploração ---
        composable(Screen.EXPLORE_MAP) {
            ExploreMapScreen(
                onBackToListView = { navController.popBackStack() }, // Volta para o Explore (Lista)
                onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) }
            )
        }
    }
}