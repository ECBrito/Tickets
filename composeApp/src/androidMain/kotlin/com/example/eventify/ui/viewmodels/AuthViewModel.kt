package com.example.eventify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.auth.AuthService
import com.example.eventify.auth.AuthState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // Instancia o serviço de autenticação
    private val authService = AuthService()

    // Observa o estado de autenticação do serviço
    val authState: StateFlow<AuthState> = authService.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Initial
        )

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authService.signIn(email, password)
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            authService.signUp(email, password)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authService.signOutSuspend()
        }
    }
}
