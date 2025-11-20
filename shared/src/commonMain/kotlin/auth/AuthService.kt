package com.example.eventify.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthResult
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.MainScope

sealed class AuthState {
    object Initial : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

// O KMP usa esta classe para gerir o estado de autenticação real com o Firebase
class AuthService(
    private val firebaseAuth: FirebaseAuth = Firebase.auth
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // Observa o estado de autenticação do Firebase e emite para o Flow
        // Isto garante que se o utilizador fechar a app e voltar, o estado é mantido
        firebaseAuth.authStateChanges.onEach { user ->
            _authState.value = if (user != null) AuthState.Authenticated else AuthState.Unauthenticated
        }.launchIn(MainScope())
    }

    suspend fun signIn(email: String, password: String): AuthResult? {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password)
            result // Se for bem-sucedido, o authState muda automaticamente
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Falha no Login: Email ou palavra-passe incorretos.")
            null
        }
    }

    suspend fun signUp(email: String, password: String): AuthResult? {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password)
            result
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Falha no Registo: Email já em uso ou inválido.")
            null
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }
}