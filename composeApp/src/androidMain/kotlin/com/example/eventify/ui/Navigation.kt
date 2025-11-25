package com.example.eventify.ui

import android.content.Context
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
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
import com.example.eventify.ui.screens.organizer.AttendeesListScreen
import com.example.eventify.ui.screens.organizer.CreateEventScreen
import com.example.eventify.ui.screens.organizer.EditEventScreen
import com.example.eventify.ui.screens.organizer.OrganizerDashboardScreen
import com.example.eventify.ui.screens.organizer.OrganizerEventDashboard
import com.example.eventify.ui.screens.organizer.ScanTicketScreen
import com.example.eventify.ui.screens.PurchaseScreen
import com.example.eventify.ui.screens.TicketDetailScreen
import com.example.eventify.ui.screens.EditProfileScreen
import com.google.firebase.auth.FirebaseAuth

object Screen {
    // --- AUTH ---
    const val ONBOARDING = "onboarding"
    const val AUTH_ROOT = "auth_root"
    const val SIGN_UP = "sign_up"
    const val FORGOT_PASSWORD = "forgot_password"

    // --- MAIN APP ---
    const val HOME_ROOT = "home_root"
    const val EXPLORE_LIST = "explore_list"
    const val EXPLORE_MAP = "explore_map"
    const val NOTIFICATIONS = "notifications"
    const val EDIT_PROFILE = "edit_profile"
    const val INTERESTS = "interests"

    // --- EVENTOS & TICKETS ---
    const val EVENT_DETAIL = "event/{eventId}"
    const val PURCHASE = "purchase/{eventId}"
    const val TICKET_DETAIL = "ticket/{ticketId}/{eventTitle}"

    // --- ORGANIZER ---
    const val ORGANIZER_DASHBOARD = "organizer_dashboard"
    const val CREATE_EVENT = "create_event"
    const val EDIT_EVENT = "edit_event/{eventId}"
    const val ORGANIZER_EVENT_STATS = "organizer_event_stats/{eventId}"
    const val ATTENDEES_LIST = "attendees_list/{eventId}" // <--- FALTAVA ESTA
    const val SCANNER = "scanner"

    // --- HELPER FUNCTIONS ---
    fun eventDetail(eventId: String) = "event/$eventId"
    fun purchase(eventId: String) = "purchase/$eventId"
    fun ticketDetail(ticketId: String, eventTitle: String) = "ticket/$ticketId/$eventTitle"
    fun organizerEventStats(eventId: String) = "organizer_event_stats/$eventId"
    fun editEvent(eventId: String) = "edit_event/$eventId"
    fun attendeesList(eventId: String) = "attendees_list/$eventId" // <--- FALTAVA ESTA
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun EventifyNavHost(
    navController: NavHostController,
    context: Context
) {
    val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val seenOnboarding = sharedPref.getBoolean("seenOnboarding", false)

    val startDestination = if (seenOnboarding) Screen.AUTH_ROOT else Screen.ONBOARDING

    SharedTransitionLayout {
        NavHost(navController = navController, startDestination = startDestination) {

            // --- ONBOARDING & AUTH ---
            composable(Screen.ONBOARDING) {
                OnboardingScreen(onFinish = {
                    sharedPref.edit().putBoolean("seenOnboarding", true).apply()
                    navController.navigate(Screen.AUTH_ROOT) { popUpTo(Screen.ONBOARDING) { inclusive = true } }
                })
            }
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
                ForgotPasswordScreen(onBackClick = { navController.popBackStack() }, onSendLinkClick = { navController.popBackStack() })
            }

            // --- MAIN FLOW (USER) ---
            composable(Screen.HOME_ROOT) {
                MainScreen(
                    navController = navController,
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }

            composable(Screen.EXPLORE_LIST) {
                val viewModel = remember { AppModule.provideExploreViewModel() }
                ExploreScreen(
                    viewModel = viewModel,
                    onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) },
                    onMapClick = { navController.navigate(Screen.EXPLORE_MAP) },
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }

            composable(Screen.EXPLORE_MAP) {
                ExploreMapScreen(
                    onBackToListView = { navController.popBackStack() },
                    onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) }
                )
            }

            composable(Screen.NOTIFICATIONS) { NotificationsScreen(onBackClick = { navController.popBackStack() }) }

            composable(Screen.EDIT_PROFILE) { EditProfileScreen(navController = navController) }

            composable(Screen.INTERESTS) { InterestsScreen(navController = navController) }

            // --- DETAILS & TICKETS ---
            composable(
                route = Screen.EVENT_DETAIL,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                EventDetailScreen(
                    eventId = eventId,
                    userId = currentUserId,
                    navController = navController,
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }

            composable(Screen.PURCHASE, arguments = listOf(navArgument("eventId") { type = NavType.StringType })) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                PurchaseScreen(eventId = eventId, navController = navController)
            }

            composable(
                route = Screen.TICKET_DETAIL,
                arguments = listOf(navArgument("ticketId") { type = NavType.StringType }, navArgument("eventTitle") { type = NavType.StringType })
            ) { backStackEntry ->
                val ticketId = backStackEntry.arguments?.getString("ticketId") ?: ""
                val eventTitle = backStackEntry.arguments?.getString("eventTitle") ?: ""
                TicketDetailScreen(ticketId = ticketId, eventTitle = eventTitle, navController = navController)
            }

            // --- ORGANIZER FLOW ---
            composable(Screen.ORGANIZER_DASHBOARD) {
                OrganizerDashboardScreen(
                    onCreateEventClick = { navController.navigate(Screen.CREATE_EVENT) },
                    onEventClick = { eventId -> navController.navigate(Screen.organizerEventStats(eventId)) },
                    onScanClick = { navController.navigate(Screen.SCANNER) },
                    onEditEventClick = { eventId -> navController.navigate(Screen.editEvent(eventId)) },
                    onViewAttendeesClick = { eventId -> navController.navigate(Screen.attendeesList(eventId)) }
                )
            }
            composable(Screen.CREATE_EVENT) {
                CreateEventScreen(onBackClick = { navController.popBackStack() }, onPublishClick = { navController.popBackStack() })
            }
            composable(
                route = Screen.EDIT_EVENT,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EditEventScreen(eventId = eventId, onBackClick = { navController.popBackStack() }, onSaveClick = { navController.popBackStack() })
            }
            composable(
                route = Screen.ORGANIZER_EVENT_STATS,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                OrganizerEventDashboard(eventId = eventId, navController = navController)
            }
            composable(Screen.SCANNER) { ScanTicketScreen(navController = navController) }

            composable(
                route = Screen.ATTENDEES_LIST,
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                AttendeesListScreen(eventId = eventId, navController = navController)
            }
        }
    }
}