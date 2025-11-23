package com.example.eventify.repository

import com.example.eventify.model.Event
import com.example.eventify.model.Ticket
import kotlinx.coroutines.flow.Flow
import com.example.eventify.model.Comment
import com.example.eventify.model.TicketValidationResult
import kotlinx.serialization.InternalSerializationApi

interface EventRepository {
    val events: Flow<List<Event>>
    suspend fun addEvent(event: Event): Boolean
    suspend fun deleteEvent(eventId: String): Boolean
    suspend fun getEventsRegisteredByUser(userId: String): List<Event>
    suspend fun toggleEventRegistration(eventId: String, userId: String)
    fun searchEvents(query: String, currentList: List<Event>): List<Event>
    // Adiciona esta função à interface
    suspend fun uploadEventImage(imageBytes: ByteArray, fileName: String): String?
    suspend fun buyTickets(userId: String, event: Event, quantity: Int): Boolean
    @OptIn(InternalSerializationApi::class)
    suspend fun getUserTickets(userId: String): List<Ticket>

    suspend fun validateTicket(ticketId: String): TicketValidationResult

    fun getComments(eventId: String): Flow<List<Comment>>
    suspend fun addComment(eventId: String, comment: Comment): Boolean
}