package com.example.eventify.repository

import com.example.eventify.model.Event
import com.example.eventify.model.Ticket
import com.example.eventify.model.TicketValidationResult
import com.example.eventify.model.Comment
import com.example.eventify.model.UserProfile
import kotlinx.coroutines.flow.Flow
// Adiciona este import
import kotlinx.serialization.InternalSerializationApi

// Adiciona esta anotação para silenciar o erro
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
    suspend fun validateTicket(ticketId: String): TicketValidationResult

    // --- COMENTÁRIOS ---
    fun getComments(eventId: String): Flow<List<Comment>>
    suspend fun addComment(eventId: String, comment: Comment): Boolean

    // --- PERFIL (USER PROFILE) ---
    suspend fun getUserProfile(userId: String): UserProfile?
    suspend fun updateUserProfile(userId: String, profile: UserProfile): Boolean
    suspend fun uploadProfileImage(imageBytes: ByteArray, userId: String): String?

    // Observa a lista de IDs que o user guardou (Flow em tempo real)
    fun getFavoriteEventIds(userId: String): Flow<List<String>>

    // Adiciona ou Remove o favorito
    suspend fun toggleFavorite(userId: String, eventId: String)
}