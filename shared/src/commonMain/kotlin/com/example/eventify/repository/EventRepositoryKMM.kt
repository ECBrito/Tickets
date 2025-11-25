package com.example.eventify.repository

import com.example.eventify.model.Attendee
import com.example.eventify.model.ChatMessage
import com.example.eventify.model.Comment
import com.example.eventify.model.Event
import com.example.eventify.model.NotificationItem
import com.example.eventify.model.Ticket
import com.example.eventify.model.TicketValidationResult
import com.example.eventify.model.UserProfile
import com.example.eventify.model.PromoCode
import com.example.eventify.model.Review
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
    suspend fun incrementEventShares(eventId: String)

    // --- IMAGENS ---
    suspend fun uploadEventImage(imageBytes: ByteArray, fileName: String): String?

    // --- BILHETES & COMPRA ---
    suspend fun buyTickets(userId: String, event: Event, quantity: Int): Boolean
    suspend fun getUserTickets(userId: String): List<Ticket>
    suspend fun getTicketsForEvent(eventId: String): List<Ticket>
    suspend fun transferTicket(ticketId: String, currentUserId: String, recipientEmail: String): Boolean
    suspend fun validateTicket(ticketId: String): TicketValidationResult
    suspend fun verifyPromoCode(code: String): PromoCode?

    // --- GESTÃO DE PARTICIPANTES (ORGANIZADOR & PÚBLICO) ---
    suspend fun getEventAttendees(eventId: String): List<Attendee>
    suspend fun getPublicAttendeesPreview(eventId: String): List<Attendee>
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

    fun isFollowing(userId: String, organizerId: String): Flow<Boolean>
    suspend fun toggleFollow(userId: String, organizerId: String)

    fun getUserNotifications(userId: String): Flow<List<NotificationItem>>
    suspend fun markNotificationAsRead(userId: String, notificationId: String)
    suspend fun createTestNotification(userId: String) // Só para testares

    fun getReviews(eventId: String): Flow<List<Review>>
    suspend fun addReview(eventId: String, review: Review): Boolean

    fun getChatMessages(eventId: String): Flow<List<ChatMessage>>
    suspend fun sendChatMessage(eventId: String, message: ChatMessage): Boolean

    fun hasTicketFlow(userId: String, eventId: String): Flow<Boolean>
}