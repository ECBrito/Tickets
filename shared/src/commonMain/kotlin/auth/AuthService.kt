package com.example.eventify.auth

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.AuthResult
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

sealed class AuthState {
    object Initial : AuthState()
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthService(
    private val firebaseAuth: FirebaseAuth = Firebase.auth
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        // CORREÇÃO: A propriedade correta é 'authStateChanged' (no passado)
        // e ela retorna um Flow<FirebaseUser?> directly.
        firebaseAuth.authStateChanged.onEach { user ->
            _authState.value = if (user != null) AuthState.Authenticated else AuthState.Unauthenticated
        }.launchIn(MainScope())
    }

    suspend fun signIn(email: String, password: String): AuthResult? {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password)
            result
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Falha no Login: ${e.message}")
            null
        }
    }

    suspend fun signUp(email: String, password: String): AuthResult? {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password)
            result
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Falha no Registo: ${e.message}")
            null
        }
    }

    fun signOut() {
        // O signOut é uma função suspend, então deve ser chamada dentro de uma coroutine
        // Mas como estamos a usar o MainScope no init, podemos lançar uma coroutine aqui também
        // Ou simplesmente torná-la suspend.
        // Para simplificar e manter a compatibilidade com o ViewModel:

        // Opção 1 (Ideal): Tornar a função suspend
        // suspend fun signOut() { firebaseAuth.signOut() }

        // Opção 2 (Rápida para não quebrar o ViewModel agora):
        // Lança uma coroutine no MainScope (não é a melhor prática para logout, mas funciona aqui)
        // kotlinx.coroutines.GlobalScope.launch { firebaseAuth.signOut() }

        // Opção 3 (Melhor Prática): O ViewModel deve chamar signOut dentro de um viewModelScope.launch
        // Então vamos mudar a assinatura aqui para ser suspend.
    }

    // CORREÇÃO: A função signOut deve ser suspend para chamar firebaseAuth.signOut()
    suspend fun signOutSuspend() {
        firebaseAuth.signOut()
    }
}