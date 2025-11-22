package com.example.eventify.repository

import com.example.eventify.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    val events: Flow<List<Event>>
    suspend fun addEvent(event: Event): Boolean
    suspend fun deleteEvent(eventId: String): Boolean
    suspend fun getEventsRegisteredByUser(userId: String): List<Event>
    suspend fun toggleEventRegistration(eventId: String, userId: String)
    fun searchEvents(query: String, currentList: List<Event>): List<Event>
}