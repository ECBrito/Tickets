package com.example.eventify.ui

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.eventify.di.AppModule
import com.example.eventify.ui.screens.*
import com.example.eventify.ui.screens.auth.ForgotPasswordScreen
import com.example.eventify.ui.screens.auth.SignInScreen
import com.example.eventify.ui.screens.auth.SignUpScreen
import com.example.eventify.ui.screens.organizer.CreateEventScreen
import com.example.eventify.ui.screens.organizer.OrganizerDashboardScreen
import com.google.firebase.auth.FirebaseAuth

object Screen {
    const val ONBOARDING = "onboarding"
    const val AUTH_ROOT = "auth_root"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"

    const val HOME_ROOT = "home_root"
    const val EXPLORE_LIST = "explore_list"
    const val EXPLORE_MAP = "explore_map"
    const val EVENT_DETAIL = "event/{eventId}"
    const val NOTIFICATIONS = "notifications"

    const val ORGANIZER_DASHBOARD = "organizer_dashboard"
    const val CREATE_EVENT = "create_event"

    fun eventDetail(eventId: String) = "event/$eventId"
}

@Composable
fun EventifyNavHost(
    navController: NavHostController,
    context: Context
) {
    // Verifica se o onboarding já foi visto
    val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val seenOnboarding = sharedPref.getBoolean("seenOnboarding", false)

    val startDestination = if (seenOnboarding) {
        Screen.AUTH_ROOT
    } else {
        Screen.ONBOARDING
    }

    NavHost(navController = navController, startDestination = startDestination) {

        // --- ONBOARDING ---
        composable(Screen.ONBOARDING) {
            OnboardingScreen(onFinish = {
                sharedPref.edit().putBoolean("seenOnboarding", true).apply()
                navController.navigate(Screen.AUTH_ROOT) {
                    popUpTo(Screen.ONBOARDING) { inclusive = true }
                }
            })
        }

        // --- AUTH FLOW ---
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

        // --- MAIN FLOW (USER) ---

        // 1. HOME ROOT: Agora aponta para o MainScreen (que tem a barra de navegação)
        composable(Screen.HOME_ROOT) {
            MainScreen(navController = navController)
        }

        // 2. Explore List Screen (Acesso direto se necessário)
        composable(Screen.EXPLORE_LIST) {
            val viewModel = remember { AppModule.provideExploreViewModel() }
            ExploreScreen(
                viewModel = viewModel,
                onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) }
            )
        }

        // 3. Explore Map Screen (Mapa)
        composable(Screen.EXPLORE_MAP) {
            ExploreMapScreen(
                onBackToListView = { navController.popBackStack() },
                onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) }
            )
        }

        // 4. Event Detail
        composable(
            route = Screen.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            EventDetailScreen(
                eventId = eventId,
                userId = currentUserId,
                navController = navController
            )
        }

        composable(Screen.NOTIFICATIONS) {
            NotificationsScreen(onBackClick = { navController.popBackStack() })
        }

        // --- ORGANIZER FLOW ---

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
    }
}