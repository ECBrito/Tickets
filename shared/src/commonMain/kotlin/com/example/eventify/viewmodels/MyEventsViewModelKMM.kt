package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyEventsViewModelKMM(
    private val repository: EventRepository,
    private val userId: String
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 1. Lista de Eventos onde sou PARTICIPANTE
    private val _registeredEvents = MutableStateFlow<List<Event>>(emptyList())
    val registeredEvents: StateFlow<List<Event>> = _registeredEvents.asStateFlow()

    // 2. Lista de Eventos onde sou ORGANIZADOR
    private val _hostedEvents = MutableStateFlow<List<Event>>(emptyList())
    val hostedEvents: StateFlow<List<Event>> = _hostedEvents.asStateFlow()

    init {
        observeMyEvents()
    }

    private fun observeMyEvents() {
        viewModelScope.launch {
            _isLoading.value = true

            // Escutamos todos os eventos e filtramos localmente
            repository.events.collect { allEvents ->

                // Filtra eventos onde estou inscrito
                val registered = allEvents.filter { event ->
                    event.registeredUserIds.contains(userId)
                }.map { it.copy(isRegistered = true) }

                _registeredEvents.value = registered

                // Filtra eventos que EU criei (Hosted)
                // NOTA: Como o nosso modelo Event atual ainda não tem "creatorId",
                // vou deixar esta lista vazia ou igual à registered por enquanto.
                // Futuramente deves adicionar 'val creatorId: String' ao modelo Event.
                _hostedEvents.value = emptyList() // TODO: Filtrar por event.creatorId == userId

                _isLoading.value = false
            }
        }
    }
}