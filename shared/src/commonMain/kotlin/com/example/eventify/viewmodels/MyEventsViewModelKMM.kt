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

    // Lista de Bilhetes (Mapeados como Eventos para a UI)
    private val _registeredEvents = MutableStateFlow<List<Event>>(emptyList())
    val registeredEvents: StateFlow<List<Event>> = _registeredEvents.asStateFlow()

    private val _hostedEvents = MutableStateFlow<List<Event>>(emptyList())
    val hostedEvents: StateFlow<List<Event>> = _hostedEvents.asStateFlow()

    init {
        loadMyEvents()
    }

    @OptIn(InternalSerializationApi::class)
    private fun loadMyEvents() {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. CORREÇÃO: Buscar os TICKETS em vez de filtrar eventos
            val myTickets = repository.getUserTickets(userId)

            // 2. Transformar Tickets em "Eventos Visuais" para a UI
            val eventsFromTickets = myTickets.map { ticket ->
                Event(
                    // TRUQUE IMPORTANTE:
                    // Usamos o ID do TICKET aqui para que, ao clicar, saibamos qual bilhete abrir
                    id = ticket.id,

                    // O resto dos dados vem do snapshot que guardámos no bilhete
                    title = ticket.eventTitle,
                    location = ticket.eventLocation,
                    imageUrl = ticket.eventImage,
                    dateTime = ticket.eventDate,
                    isRegistered = true
                )
            }

            _registeredEvents.value = eventsFromTickets

            // Para Hosted (Organizador), mantemos a lógica ou deixamos vazio por agora
            _hostedEvents.value = emptyList()

            _isLoading.value = false
        }
    }
}