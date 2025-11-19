package com.example.eventify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventDetailViewModel : ViewModel() {

    private val repository = EventRepository

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val foundEvent = repository.getEventById(eventId)
            _event.value = foundEvent
            _isLoading.value = false
        }
    }

    // NOVA FUNÇÃO: Lógica de RSVP
    fun toggleRsvp() {
        val currentEvent = _event.value ?: return

        viewModelScope.launch {
            // Atualiza no repositório
            repository.toggleEventRegistration(currentEvent.id)

            // Recarrega o evento para atualizar a UI (botão mudar de cor/texto)
            val updatedEvent = repository.getEventById(currentEvent.id)
            _event.value = updatedEvent
        }
    }
}