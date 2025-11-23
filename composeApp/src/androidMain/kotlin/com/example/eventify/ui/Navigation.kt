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
import com.example.eventify.ui.screens.organizer.OrganizerEventDashboard
import com.example.eventify.ui.screens.organizer.ScanTicketScreen // <--- IMPORT NOVO
import com.example.eventify.ui.screens.PurchaseScreen
import com.example.eventify.ui.screens.TicketDetailScreen
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

    // --- EVENTOS & TICKETS ---
    const val EVENT_DETAIL = "event/{eventId}"
    const val PURCHASE = "purchase/{eventId}"
    const val TICKET_DETAIL = "ticket/{ticketId}/{eventTitle}"

    // --- ORGANIZER ---
    const val ORGANIZER_DASHBOARD = "organizer_dashboard"
    const val CREATE_EVENT = "create_event"
    const val ORGANIZER_EVENT_STATS = "organizer_event_stats/{eventId}"
    const val SCANNER = "scanner" // <--- CONSTANTE NOVA QUE FALTAVA

    // --- HELPER FUNCTIONS ---
    fun eventDetail(eventId: String) = "event/$eventId"
    fun purchase(eventId: String) = "purchase/$eventId"
    fun ticketDetail(ticketId: String, eventTitle: String) = "ticket/$ticketId/$eventTitle"
    fun organizerEventStats(eventId: String) = "organizer_event_stats/$eventId"
}

@Composable
fun EventifyNavHost(
    navController: NavHostController,
    context: Context
) {
    val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    val seenOnboarding = sharedPref.getBoolean("seenOnboarding", false)

    val startDestination = if (seenOnboarding) Screen.AUTH_ROOT else Screen.ONBOARDING

    NavHost(navController = navController, startDestination = startDestination) {

        // ... (Onboarding, Auth, Main Flow mantêm-se iguais) ...
        // Vou omitir o código repetido para poupar espaço, mas mantém o que tinhas antes
        // até chegar à parte do Organizer Flow.

        // (Se copiares o ficheiro todo da resposta anterior,
        // apenas adiciona o import ScanTicketScreen e o const SCANNER lá em cima)

        // --- REPETINDO O BLOCO AUTH/MAIN PARA GARANTIR INTEGRIDADE SE COPIARES TUDO ---
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
        composable(Screen.HOME_ROOT) { MainScreen(navController = navController) }
        composable(Screen.EXPLORE_LIST) {
            val viewModel = remember { AppModule.provideExploreViewModel() }
            ExploreScreen(
                viewModel = viewModel,
                onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) },
                onMapClick = { navController.navigate(Screen.EXPLORE_MAP) }
            )
        }
        composable(Screen.EXPLORE_MAP) {
            ExploreMapScreen(
                onBackToListView = { navController.popBackStack() },
                onEventClick = { eventId -> navController.navigate(Screen.eventDetail(eventId)) }
            )
        }
        composable(Screen.NOTIFICATIONS) { NotificationsScreen(onBackClick = { navController.popBackStack() }) }
        composable(
            route = Screen.EVENT_DETAIL,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            EventDetailScreen(eventId = eventId, userId = currentUserId, navController = navController)
        }
        composable(
            route = Screen.PURCHASE,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
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

        // =====================================================================
        // ORGANIZER FLOW (ATUALIZADO)
        // =====================================================================

        // 1. Dashboard Principal
        composable(Screen.ORGANIZER_DASHBOARD) {
            OrganizerDashboardScreen(
                onCreateEventClick = { navController.navigate(Screen.CREATE_EVENT) },
                onEventClick = { eventId -> navController.navigate(Screen.organizerEventStats(eventId)) },
                // NOVO: Callback para abrir o scanner
                onScanClick = { navController.navigate(Screen.SCANNER) }
            )
        }

        // 2. Criar Evento
        composable(Screen.CREATE_EVENT) {
            CreateEventScreen(
                onBackClick = { navController.popBackStack() },
                onPublishClick = { navController.popBackStack() }
            )
        }

        // 3. Estatísticas do Evento
        composable(
            route = Screen.ORGANIZER_EVENT_STATS,
            arguments = listOf(navArgument("eventId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
            OrganizerEventDashboard(
                eventId = eventId,
                navController = navController
            )
        }

        // 4. SCANNER (A rota que faltava)
        composable(Screen.SCANNER) {
            ScanTicketScreen(navController = navController)
        }
    }
}