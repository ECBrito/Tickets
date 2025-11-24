package com.example.eventify.repository

import com.example.eventify.model.Attendee
import com.example.eventify.model.Comment
import com.example.eventify.model.Event
import com.example.eventify.model.Ticket
import com.example.eventify.model.TicketValidationResult
import com.example.eventify.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
interface EventRepository {
    // --- EVENTOS ---
    val events: Flow<List<Event>>

    suspend fun addEvent(event: Event): Boolean
    suspend fun deleteEvent(eventId: String): Boolean
    suspend fun getEventsRegisteredByUser(userId: String): List<Event>
    suspend fun toggleEventRegistration(eventId: String, userId: String)
    fun searchEvents(query: String, currentList: List<Event>): List<Event>

    // --- IMAGENS ---
    suspend fun uploadEventImage(imageBytes: ByteArray, fileName: String): String?

    // --- BILHETES ---
    suspend fun buyTickets(userId: String, event: Event, quantity: Int): Boolean
    suspend fun getUserTickets(userId: String): List<Ticket>
    suspend fun transferTicket(ticketId: String, currentUserId: String, recipientEmail: String): Boolean
    suspend fun validateTicket(ticketId: String): TicketValidationResult
    // Função de Estatísticas (Faltava na interface anterior)
    suspend fun getTicketsForEvent(eventId: String): List<Ticket>

    // --- GESTÃO DE PARTICIPANTES (ORGANIZADOR) ---
    suspend fun getEventAttendees(eventId: String): List<Attendee>
    suspend fun manualCheckIn(ticketId: String): Boolean

    // --- COMENTÁRIOS ---
    fun getComments(eventId: String): Flow<List<Comment>>
    suspend fun addComment(eventId: String, comment: Comment): Boolean

    // --- PERFIL & USER ---
    suspend fun getUserProfile(userId: String): UserProfile?
    suspend fun updateUserProfile(userId: String, profile: UserProfile): Boolean
    suspend fun uploadProfileImage(imageBytes: ByteArray, userId: String): String?
    suspend fun updateUserFcmToken(userId: String, token: String)

    // --- FAVORITOS ---
    fun getFavoriteEventIds(userId: String): Flow<List<String>>
    suspend fun toggleFavorite(userId: String, eventId: String)

    // --- PARTILHAS (SOCIAL) ---
    // Esta é a função que estava a dar erro de override
    suspend fun incrementEventShares(eventId: String)
}