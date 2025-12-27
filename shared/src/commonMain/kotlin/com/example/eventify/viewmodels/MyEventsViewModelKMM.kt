package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
class MyEventsViewModelKMM(
    private val repository: EventRepository,
    private val userId: String
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 1. Lista de BILHETES (Tickets comprados)
    private val _registeredEvents = MutableStateFlow<List<Event>>(emptyList())
    val registeredEvents: StateFlow<List<Event>> = _registeredEvents.asStateFlow()

    // 2. Lista de FAVORITOS (Bookmarks)
    // Combinamos "Todos os Eventos" com "Meus IDs Favoritos" para filtrar
    val favoriteEvents: StateFlow<List<Event>> = combine(
        repository.events,
        repository.getFavoriteEventIds(userId)
    ) { allEvents, favIds ->
        allEvents.filter { event ->
            favIds.contains(event.id)
        }.map {
            it.copy(isSaved = true) // Garante que o ícone aparece preenchido
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadMyTickets()

        // Monitoriza o loading dos favoritos também
        viewModelScope.launch {
            favoriteEvents.collect { _isLoading.value = false }
        }
    }

    @OptIn(InternalSerializationApi::class)
    private fun loadMyTickets() {
        viewModelScope.launch {
            _isLoading.value = true
            val myTickets = repository.getUserTickets(userId)

            val eventsFromTickets = myTickets.map { ticket ->
                Event(
                    id = ticket.id, // ID do Bilhete
                    title = ticket.eventTitle,
                    locationName = ticket.eventLocation,
                    imageUrl = ticket.eventImage,
                    dateTime = ticket.eventDate,
                    isRegistered = true
                )
            }
            _registeredEvents.value = eventsFromTickets
            // Não desligamos o loading aqui porque esperamos pelos favoritos também
        }
    }

    // Permitir remover dos favoritos diretamente neste ecrã
    fun removeFavorite(eventId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(userId, eventId)
        }
    }
}