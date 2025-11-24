package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Comment
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.InternalSerializationApi

class EventDetailViewModelKMM(
    private val repository: EventRepository,
    private val eventId: String,
    private val userId: String
) : ViewModel() {

    @OptIn(InternalSerializationApi::class)
    private val _event = MutableStateFlow<Event?>(null)
    @OptIn(InternalSerializationApi::class)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // --- NOVO: Lista de Comentários ---
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    init {
        observeEvent()
        observeComments() // <--- Começa a escutar comentários
    }

    @OptIn(InternalSerializationApi::class)
    private fun observeEvent() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.events.collect { eventsList ->
                val foundEvent = eventsList.find { it.id == eventId }
                val eventWithStatus = foundEvent?.copy(
                    isRegistered = foundEvent.registeredUserIds.contains(userId)
                )
                _event.value = eventWithStatus
                _isLoading.value = false
            }
        }
    }

    // --- NOVO: Escutar Comentários ---
    private fun observeComments() {
        viewModelScope.launch {
            repository.getComments(eventId).collect { list ->
                _comments.value = list
            }
        }
    }

    fun toggleRsvp() {
        viewModelScope.launch {
            repository.toggleEventRegistration(eventId, userId)
        }
    }

    // --- NOVO: Enviar Comentário ---
    fun sendComment(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            val currentUser = Firebase.auth.currentUser
            val userName = currentUser?.displayName ?: "User" // Fallback se não tiver nome
            // Nota: Se tiveres foto no perfil, usa currentUser?.photoURL

            val newComment = Comment(
                userId = userId,
                userName = userName,
                text = text,
                timestamp = Clock.System.now().toEpochMilliseconds()
            )
            repository.addComment(eventId, newComment)
        }
    }
    fun registerShare() {
        viewModelScope.launch {
            repository.incrementEventShares(eventId)
            // O Flow do evento atualiza-se automaticamente,
            // por isso o número de shares vai subir na UI sozinho.
        }
    }

    @OptIn(InternalSerializationApi::class)
    val isRegistered: Boolean
        get() = _event.value?.isRegistered == true
}