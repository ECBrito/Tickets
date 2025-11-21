package com.example.eventify.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.eventify.ui.screens.*
import com.example.eventify.ui.screens.auth.ForgotPasswordScreen
import com.example.eventify.ui.screens.auth.SignInScreen
import com.example.eventify.ui.screens.auth.SignUpScreen
import com.example.eventify.ui.screens.organizer.CreateEventScreen
import com.example.eventify.ui.screens.organizer.OrganizerDashboardScreen
import com.google.firebase.auth.FirebaseAuth

// Rotas da aplicação
object Screen {
    const val ONBOARDING = "onboarding"
    const val AUTH_ROOT = "auth_root"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"

    const val HOME_ROOT = "home_root"
    const val EVENT_DETAIL = "event/{eventId}"

    const val ORGANIZER_DASHBOARD = "organizer_dashboard"
    const val CREATE_EVENT = "create_event"

    const val NOTIFICATIONS = "notifications"
    const val EXPLORE_MAP = "explore_map"

    fun eventDetail(eventId: String) = "event/$eventId"
}

@Composable
fun EventifyNavHost(
    navController: NavHostController,
    startDestination: String = Screen.ONBOARDING
) {
    NavHost(navController = navController, startDestination = startDestination) {

        // ===========================
        // Onboarding
        // ===========================
        composable(Screen.ONBOARDING) {
            OnboardingScreen(onFinish = {
                navController.navigate(Screen.AUTH_ROOT) {
                    popUpTo(Screen.ONBOARDING) { inclusive = true }
                }
            })
        }

        // ===========================
        // Auth Flow
        // ===========================
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

        // ===========================
        // Main Flow (User)
        // ===========================
        composable(Screen.HOME_ROOT) {
            HomeScreenWrapper(navController = navController)
        }

        composable(
            route = Screen.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            EventDetailScreen(
                eventId = eventId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.NOTIFICATIONS) {
            NotificationsScreen(onBackClick = { navController.popBackStack() })
        }

        // ===========================
        // Organizer Flow
        // ===========================
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        composable(Screen.ORGANIZER_DASHBOARD) {
            OrganizerDashboardScreen(
                organizerId = currentUserId,
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

        // ===========================
        // Explore Map Screen
        // ===========================
        composable(Screen.EXPLORE_MAP) {
            ExploreMapScreen(
                onBackToListView = { navController.popBackStack() },
                onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) }
            )
        }
    }
}
