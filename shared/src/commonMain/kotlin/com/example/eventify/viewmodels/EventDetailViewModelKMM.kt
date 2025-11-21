package com.example.eventify.viewmodels

import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepositoryKMM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventDetailViewModelKMM(
    private val repository: EventRepositoryKMM,
    private val eventId: String,
    private val userId: String
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadEvent()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.startListening()
            repository.events.collect { eventsList ->
                _event.value = eventsList.find { it.id == eventId }
            }
            _isLoading.value = false
        }
    }

    fun toggleRsvp() {
        viewModelScope.launch {
            _event.value?.let { currentEvent ->
                // Alterna o estado de inscrição do utilizador
                repository.toggleEventRegistration(currentEvent.id, userId)
                _event.value = currentEvent.copy(
                    isRegistered = !currentEvent.isRegistered
                )
            }
        }
    }

    val isRegistered: Boolean
        get() = _event.value?.isRegistered == true
}
