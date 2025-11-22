package com.example.eventify

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current

    if (authState == AuthState.Initial || authState is AuthState.Error) {
        // Loading
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    } else {
        // Navigation com onboarding / auth / home
        EventifyNavHost(
            navController = navController,
            context = context
        )
    }
}
