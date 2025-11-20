package com.example.eventify.repository

import com.example.eventify.data.FirestoreEventService
import com.example.eventify.model.Event
import kotlinx.coroutines.flow.Flow

/**
 * Repositório que faz a ponte entre os ViewModels e o Firestore.
 */
class EventRepository(
    // Injeta o serviço (cria um novo por defeito)
    private val eventService: FirestoreEventService = FirestoreEventService()
) {

    fun getAllEvents(): Flow<List<Event>> {
        return eventService.getAllEventsFlow()
    }

    suspend fun saveEvent(event: Event) {
        eventService.saveEvent(event)
    }

    suspend fun deleteEvent(eventId: String) {
        eventService.deleteEvent(eventId)
    }

    // Função de placeholder para manter compatibilidade com ViewModels antigos
    // Numa app real, farias esta busca diretamente no Firestore
    suspend fun getEventById(id: String): Event? {
        return null
    }

    // Função de placeholder para pesquisa
    fun searchEvents(query: String): List<Event> {
        return emptyList()
    }
}