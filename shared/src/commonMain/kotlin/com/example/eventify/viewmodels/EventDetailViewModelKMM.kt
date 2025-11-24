package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Attendee // <--- Import
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

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    // --- NOVO: Lista de Participantes ---
    @OptIn(InternalSerializationApi::class)
    private val _attendees = MutableStateFlow<List<Attendee>>(emptyList())
    @OptIn(InternalSerializationApi::class)
    val attendees: StateFlow<List<Attendee>> = _attendees.asStateFlow()

    init {
        observeEvent()
        observeComments()
        loadAttendees() // <--- Carregar a prova social
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

    private fun observeComments() {
        viewModelScope.launch {
            repository.getComments(eventId).collect { list -> _comments.value = list }
        }
    }

    // --- NOVO: Carregar Participantes ---
    @OptIn(InternalSerializationApi::class)
    private fun loadAttendees() {
        viewModelScope.launch {
            // Buscamos todos (o filtro visual fazemos na UI)
            val list = repository.getEventAttendees(eventId)
            _attendees.value = list
        }
    }

    fun toggleRsvp() {
        viewModelScope.launch {
            repository.toggleEventRegistration(eventId, userId)
        }
    }

    fun sendComment(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val currentUser = Firebase.auth.currentUser
            val userName = currentUser?.displayName ?: "User"
            val newComment = Comment(
                userId = userId,
                userName = userName,
                text = text,
                timestamp = Clock.System.now().toEpochMilliseconds(),
                userPhotoUrl = currentUser?.photoURL
            )
            repository.addComment(eventId, newComment)
        }
    }

    fun registerShare() {
        viewModelScope.launch { repository.incrementEventShares(eventId) }
    }

    @OptIn(InternalSerializationApi::class)
    val isRegistered: Boolean
        get() = _event.value?.isRegistered == true
}