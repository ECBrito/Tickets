package com.example.eventify.repository

import com.example.eventify.model.Event
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class IOSDummyEventRepository : EventRepositoryKMM {

    private val _eventsFlow = MutableStateFlow<List<Event>>(emptyList())
    override val events: StateFlow<List<Event>> = _eventsFlow

    override suspend fun startListening() {
        // Apenas exemplo: inicializa com alguns eventos dummy
        _eventsFlow.value = listOf(
            Event(
                id = "1",
                title = "iOS Sample Event",
                description = "This is a sample event for iOS",
                category = com.example.eventify.model.EventCategory.OTHER,
                location = "Lisbon",
                date = "2025-11-20",
                imageUrl = null,
                organizerId = "ios_organizer",
                isRegistered = false
            )
        )
    }

    override fun stopListening() {
        // iOS dummy não precisa remover listener
    }

    override suspend fun addEvent(event: Event): Boolean {
        _eventsFlow.value = _eventsFlow.value + event
        return true
    }

    override suspend fun deleteEvent(eventId: String): Boolean {
        _eventsFlow.value = _eventsFlow.value.filterNot { it.id == eventId }
        return true
    }

    override suspend fun toggleEventRegistration(eventId: String) {
        val updatedList = _eventsFlow.value.map { event ->
            if (event.id == eventId) event.copy(isRegistered = !event.isRegistered)
            else event
        }
        _eventsFlow.value = updatedList
    }

    override fun getEventById(id: String): Event? =
        _eventsFlow.value.find { it.id == id }

    override fun searchEvents(query: String): List<Event> {
        if (query.isBlank()) return _eventsFlow.value
        return _eventsFlow.value.filter { event ->
            event.title.contains(query, ignoreCase = true) ||
            event.description.contains(query, ignoreCase = true) ||
            event.location.contains(query, ignoreCase = true)
        }
    }
}
