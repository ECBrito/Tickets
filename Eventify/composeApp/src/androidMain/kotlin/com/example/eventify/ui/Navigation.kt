package com.example.eventify.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.eventify.ui.screens.EventDetailScreen
import com.example.eventify.ui.screens.HomeScreenWrapper
import com.example.eventify.ui.screens.OnboardingScreen // Certifica-te de que tens este import
import com.example.eventify.ui.screens.auth.ForgotPasswordScreen
import com.example.eventify.ui.screens.auth.SignInScreen
import com.example.eventify.ui.screens.auth.SignUpScreen
import com.example.eventify.ui.screens.organizer.CreateEventScreen
import com.example.eventify.ui.screens.organizer.OrganizerDashboardScreen

// Objeto de Rotas (para usar em toda a app)
object Screen {
    const val ONBOARDING = "onboarding"
    const val AUTH_ROOT = "auth_root" // Tela de Login
    const val SIGN_UP = "sign_up"     // Tela de Registo
    const val FORGOT_PASSWORD = "forgot_password"

    const val HOME_ROOT = "home_root" // Tela Principal (Feed com abas)
    const val EVENT_DETAIL = "event/{eventId}"

    const val ORGANIZER_DASHBOARD = "organizer_dashboard" // Dashboard do Organizador
    const val CREATE_EVENT = "create_event"               // Criar Evento

    // Helper para criar a rota com argumento
    fun eventDetail(eventId: String): String = "event/$eventId"
}

@Composable
fun EventifyNavHost(
    navController: NavHostController,
    startDestination: String = Screen.ONBOARDING // Começa no Onboarding
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // ==========================================
        // 0. Onboarding (A PARTE QUE FALTA)
        // ==========================================
        composable(Screen.ONBOARDING) {
            OnboardingScreen(
                onFinish = {
                    // Quando terminar o onboarding, vai para o Login e remove o onboarding da pilha
                    navController.navigate(Screen.AUTH_ROOT) {
                        popUpTo(Screen.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        // ==========================================
        // 1. Fluxo de Autenticação
        // ==========================================

        composable(Screen.AUTH_ROOT) {
            SignInScreen(
                onSignInClick = {
                    // Ao fazer login com sucesso, navega para a Home e limpa a pilha
                    navController.navigate(Screen.HOME_ROOT) {
                        popUpTo(Screen.AUTH_ROOT) { inclusive = true }
                    }
                },
                onForgotPasswordClick = {
                    navController.navigate(Screen.FORGOT_PASSWORD)
                },
                onSignUpClick = {
                    navController.navigate(Screen.SIGN_UP)
                }
            )
        }

        composable(Screen.SIGN_UP) {
            SignUpScreen(
                onSignUpClick = {
                    // Simula registo bem-sucedido indo para a Home
                    navController.navigate(Screen.HOME_ROOT) {
                        popUpTo(Screen.AUTH_ROOT) { inclusive = true }
                    }
                },
                onSignInClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() },
                onSendLinkClick = { email ->
                    // Simula envio e volta atrás
                    navController.popBackStack()
                }
            )
        }

        // ==========================================
        // 2. Fluxo Principal (Utilizador)
        // ==========================================

        // Home Wrapper (Contém as abas: Home, Explore, My Events, Profile)
        composable(Screen.HOME_ROOT) {
            HomeScreenWrapper(navController = navController)
        }

        // Detalhes do Evento (Com argumento eventId)
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

        // ==========================================
        // 3. Fluxo do Organizador
        // ==========================================

        composable(Screen.ORGANIZER_DASHBOARD) {
            OrganizerDashboardScreen(
                onCreateEventClick = {
                    navController.navigate(Screen.CREATE_EVENT)
                },
                onEventClick = { eventId ->
                    navController.navigate(Screen.eventDetail(eventId))
                }
            )
        }

        composable(Screen.CREATE_EVENT) {
            CreateEventScreen(
                onBackClick = { navController.popBackStack() },
                onPublishClick = {
                    // Simula publicação e volta para o dashboard
                    navController.popBackStack()
                }
            )
        }
    }
}