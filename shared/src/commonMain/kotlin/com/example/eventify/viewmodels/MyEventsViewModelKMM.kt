package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi

class MyEventsViewModelKMM(
    private val repository: EventRepository,
    private val userId: String
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Apenas uma lista: Os bilhetes comprados
    @OptIn(InternalSerializationApi::class)
    private val _registeredEvents = MutableStateFlow<List<Event>>(emptyList())
    @OptIn(InternalSerializationApi::class)
    val registeredEvents: StateFlow<List<Event>> = _registeredEvents.asStateFlow()

    init {
        loadMyTickets()
    }

    @OptIn(InternalSerializationApi::class)
    private fun loadMyTickets() {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. Buscar TICKETS
            val myTickets = repository.getUserTickets(userId)

            // 2. Transformar em Eventos para a UI
            val eventsFromTickets = myTickets.map { ticket ->
                Event(
                    id = ticket.id, // ID do bilhete (para abrir o QR Code)
                    title = ticket.eventTitle,
                    location = ticket.eventLocation,
                    imageUrl = ticket.eventImage,
                    dateTime = ticket.eventDate,
                    isRegistered = true
                )
            }

            _registeredEvents.value = eventsFromTickets
            _isLoading.value = false
        }
    }
}