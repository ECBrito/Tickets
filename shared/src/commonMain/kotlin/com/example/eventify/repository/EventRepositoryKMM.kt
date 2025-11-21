package com.example.eventify.repository

import com.example.eventify.model.Event
import kotlinx.coroutines.flow.StateFlow

interface EventRepositoryKMM {
    val events: StateFlow<List<Event>>

    suspend fun startListening()
    fun stopListening()

    suspend fun addEvent(event: Event): Boolean
    suspend fun deleteEvent(eventId: String): Boolean
    fun getEventById(id: String): Event?
    fun searchEvents(query: String): List<Event>
    suspend fun getEventsRegisteredByUser(userId: String): List<Event>
    suspend fun toggleEventRegistration(eventId: String, userId: String)
}
