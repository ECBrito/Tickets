package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Import crucial
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository // Interface correta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EventDetailViewModelKMM(
    private val repository: EventRepository,
    private val eventId: String,
    private val userId: String
) : ViewModel() { // 1. Herança correta

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeEvent()
    }

    // 2. Observar o evento em tempo real
    private fun observeEvent() {
        viewModelScope.launch {
            _isLoading.value = true

            // Escutamos a lista global de eventos
            repository.events.collect { eventsList ->
                // Encontramos o evento específico
                val foundEvent = eventsList.find { it.id == eventId }

                // 3. Lógica de mapeamento para saber se o user está inscrito
                // O repositório traz a lista de IDs, aqui verificamos se o 'userId' atual está lá.
                val eventWithStatus = foundEvent?.copy(
                    isRegistered = foundEvent.registeredUserIds.contains(userId)
                )

                _event.value = eventWithStatus
                _isLoading.value = false
            }
        }
    }

    fun toggleRsvp() {
        // Não precisas de atualizar o estado local manualmente (_event.value = ...).
        // Ao chamar o repository, o Firebase atualiza, o Flow dispara de novo,
        // e o método 'observeEvent' acima atualiza a UI automaticamente.
        viewModelScope.launch {
            repository.toggleEventRegistration(eventId, userId)
        }
    }

    // Helper para a UI saber se está inscrito (lê do estado atual)
    val isRegistered: Boolean
        get() = _event.value?.isRegistered == true
}