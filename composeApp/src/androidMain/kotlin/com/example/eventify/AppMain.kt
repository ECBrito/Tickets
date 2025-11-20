package com.example.eventify

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.eventify.auth.AuthState
import com.example.eventify.ui.EventifyNavHost
import com.example.eventify.ui.Screen
import com.example.eventify.ui.viewmodels.AuthViewModel

@Composable
fun AppMain(
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val navController = rememberNavController()

    // Lógica de decisão do ecrã inicial
    val startRoute = when (authState) {
        AuthState.Authenticated -> Screen.HOME_ROOT
        AuthState.Unauthenticated -> Screen.AUTH_ROOT
        // Se estiver a carregar ou der erro inicial, ficamos num estado neutro (null)
        AuthState.Initial, is AuthState.Error -> null
    }

    if (startRoute == null) {
        // Ecrã de Loading enquanto o Firebase inicializa
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        // Se já tivermos decisão, iniciamos a navegação
        // Nota: Se quisermos forçar o Onboarding na primeira vez, teríamos de guardar essa flag localmente.
        // Por agora, se não estiver autenticado, vai para o Auth (Login), que tem link para SignUp.
        EventifyNavHost(
            navController = navController,
            startDestination = startRoute
        )
    }
}