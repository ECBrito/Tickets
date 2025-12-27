package com.example.eventify.repository

import com.example.eventify.model.*
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
class EventRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : EventRepository {

    private val eventsCollection = firestore.collection("events")
    private val ticketsCollection = firestore.collection("tickets")
    private val usersCollection = firestore.collection("users")
    private val promoCodesCollection = firestore.collection("promocodes")

    // --- 1. GESTÃO DE EVENTOS ---

    override val events: Flow<List<Event>> = eventsCollection.snapshots.map { snapshot ->
        snapshot.documents.mapNotNull { doc ->
            try {
                val event = doc.data<Event>()
                event.copy(id = doc.id)
            } catch (e: Exception) {
                println("Erro ao serializar evento: ${e.message}")
                null
            }
        }
    }

    override suspend fun addEvent(event: Event): Boolean {
        return try {
            if (event.id.isNotEmpty()) {
                // Atualiza evento existente
                eventsCollection.document(event.id).set(event, merge = true)
            } else {
                // Cria novo evento e garante que o ID gerado pelo Firebase é salvo no objeto
                val newDoc = eventsCollection.document
                val eventWithId = event.copy(id = newDoc.id)
                newDoc.set(eventWithId)
            }
            true
        } catch (e: Exception) {
            println("Erro ao adicionar evento: ${e.message}")
            false
        }
    }

    override suspend fun deleteEvent(eventId: String): Boolean {
        return try {
            eventsCollection.document(eventId).delete()
            true
        } catch (e: Exception) { false }
    }

    override suspend fun getEventsRegisteredByUser(userId: String): List<Event> {
        return try {
            val querySnapshot = eventsCollection.where { "registeredUserIds" contains userId }.get()
            querySnapshot.documents.mapNotNull { doc ->
                try {
                    doc.data<Event>().copy(id = doc.id, isRegistered = true)
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun toggleEventRegistration(eventId: String, userId: String) {
        try {
            val docRef = eventsCollection.document(eventId)
            val snapshot = docRef.get()
            val currentUsers = try {
                snapshot.get<List<String>>("registeredUserIds").toMutableList()
            } catch (e: Exception) { mutableListOf() }

            if (currentUsers.contains(userId)) currentUsers.remove(userId) else currentUsers.add(userId)
            docRef.update("registeredUserIds" to currentUsers)
        } catch (e: Exception) { }
    }

    override fun searchEvents(query: String, currentList: List<Event>): List<Event> {
        if (query.isBlank()) return currentList
        return currentList.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.locationName.contains(query, ignoreCase = true) // Corrigido para locationName
        }
    }

    override suspend fun incrementEventShares(eventId: String) {
        try {
            val docRef = eventsCollection.document(eventId)
            val event = docRef.get().data<Event>()
            docRef.update("shares" to event.shares + 1)
        } catch (e: Exception) { }
    }

    // --- 2. MULTIMÉDIA (STORAGE) ---

    override suspend fun uploadEventImage(imageBytes: ByteArray, fileName: String): String? {
        return try {
            val storageRef = storage.reference.child("event_images/$fileName")
            val dataObj = Data(imageBytes)
            storageRef.putData(dataObj)
            storageRef.getDownloadUrl()
        } catch (e: Exception) { null }
    }

    override suspend fun uploadProfileImage(imageBytes: ByteArray, userId: String): String? {
        return try {
            val storageRef = storage.reference.child("profile_images/$userId.jpg")
            val dataObj = Data(imageBytes)
            storageRef.putData(dataObj)
            storageRef.getDownloadUrl()
        } catch (e: Exception) { null }
    }

    // --- 3. BILHÉTICA & COMPRA ---

    override suspend fun buyTickets(userId: String, event: Event, quantity: Int): Boolean {
        return try {
            val batch = firestore.batch()
            repeat(quantity) {
                val newDoc = ticketsCollection.document
                val ticket = Ticket(
                    id = newDoc.id,
                    userId = userId,
                    eventId = event.id,
                    eventTitle = event.title,
                    eventLocation = event.locationName, // Corrigido para locationName
                    eventDate = event.dateTime,
                    eventImage = event.imageUrl,
                    purchaseDate = Clock.System.now().toEpochMilliseconds(),
                    isValid = true
                )
                batch.set(newDoc, ticket)
            }
            batch.commit()
            true
        } catch (e: Exception) { false }
    }

    override suspend fun getUserTickets(userId: String): List<Ticket> {
        return try {
            val snapshot = ticketsCollection.where { "userId" equalTo userId }.get()
            snapshot.documents.mapNotNull { doc ->
                try {
                    val ticket = doc.data<Ticket>()
                    ticket.copy(id = doc.id)
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getTicketsForEvent(eventId: String): List<Ticket> {
        return try {
            val snapshot = ticketsCollection.where { "eventId" equalTo eventId }.get()
            snapshot.documents.mapNotNull { doc ->
                try { doc.data<Ticket>().copy(id = doc.id) } catch (e: Exception) { null }
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun transferTicket(ticketId: String, currentUserId: String, recipientEmail: String): Boolean {
        return try {
            val userQuery = usersCollection.where { "email" equalTo recipientEmail }.get()
            if (userQuery.documents.isEmpty()) return false

            val recipientId = userQuery.documents.first().id
            val ticketDoc = ticketsCollection.document(ticketId)
            val ticketSnapshot = ticketDoc.get()

            if (!ticketSnapshot.exists) return false
            val ownerId = ticketSnapshot.get<String>("userId")
            if (ownerId != currentUserId) return false

            ticketDoc.update("userId" to recipientId)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun validateTicket(ticketId: String): TicketValidationResult {
        return try {
            val docRef = ticketsCollection.document(ticketId)
            val snapshot = docRef.get()
            if (!snapshot.exists) return TicketValidationResult.INVALID
            val isValid = snapshot.get<Boolean>("isValid")
            if (isValid) {
                docRef.update("isValid" to false)
                TicketValidationResult.VALID
            } else {
                TicketValidationResult.ALREADY_USED
            }
        } catch (e: Exception) { TicketValidationResult.ERROR }
    }

    override suspend fun verifyPromoCode(code: String): PromoCode? {
        return try {
            val doc = promoCodesCollection.document(code.uppercase()).get()
            if (doc.exists) {
                val promo = doc.data<PromoCode>()
                if (promo.isActive) promo else null
            } else null
        } catch (e: Exception) { null }
    }

    // --- 4. PARTICIPANTES & CHECK-IN ---

    override suspend fun getEventAttendees(eventId: String): List<Attendee> {
        return try {
            val ticketsSnapshot = ticketsCollection.where { "eventId" equalTo eventId }.get()
            val tickets = ticketsSnapshot.documents.mapNotNull { doc ->
                try {
                    Ticket(id = doc.id, userId = doc.get("userId"), isValid = doc.get("isValid"))
                } catch (e: Exception) { null }
            }

            tickets.mapNotNull { ticket ->
                val userProfile = getUserProfile(ticket.userId)
                if (userProfile != null) {
                    Attendee(
                        ticketId = ticket.id,
                        userId = userProfile.id,
                        name = userProfile.name.ifBlank { "User" },
                        email = userProfile.email,
                        photoUrl = userProfile.photoUrl,
                        isCheckedIn = !ticket.isValid,
                        isPublic = userProfile.isPublic
                    )
                } else null
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getPublicAttendeesPreview(eventId: String): List<Attendee> {
        val allAttendees = getEventAttendees(eventId)
        return allAttendees.filter { it.isPublic && it.photoUrl.isNotBlank() }
    }

    override suspend fun manualCheckIn(ticketId: String): Boolean {
        return try {
            ticketsCollection.document(ticketId).update("isValid" to false)
            true
        } catch (e: Exception) { false }
    }

    // --- 5. SOCIAL (COMENTÁRIOS, REVIEWS, CHAT) ---

    override fun getComments(eventId: String): Flow<List<Comment>> {
        return eventsCollection.document(eventId).collection("comments").snapshots.map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                try { doc.data<Comment>().copy(id = doc.id) } catch (e: Exception) { null }
            }.sortedByDescending { it.timestamp }
        }
    }

    override suspend fun addComment(eventId: String, comment: Comment): Boolean {
        return try {
            eventsCollection.document(eventId).collection("comments").add(comment)
            true
        } catch (e: Exception) { false }
    }

    override fun getReviews(eventId: String): Flow<List<Review>> {
        return eventsCollection.document(eventId).collection("reviews").snapshots.map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                try { doc.data<Review>().copy(id = doc.id) } catch (e: Exception) { null }
            }.sortedByDescending { it.timestamp }
        }
    }

    override suspend fun addReview(eventId: String, review: Review): Boolean {
        return try {
            val eventRef = eventsCollection.document(eventId)
            eventRef.collection("reviews").add(review)

            val eventSnapshot = eventRef.get()
            val currentRating = try { eventSnapshot.get<Double>("rating") } catch(e: Exception) { 0.0 }
            val currentCount = try { eventSnapshot.get<Int>("reviewCount") } catch(e: Exception) { 0 }

            val newCount = currentCount + 1
            val newRating = ((currentRating * currentCount) + review.rating) / newCount

            eventRef.update("rating" to newRating, "reviewCount" to newCount)
            true
        } catch (e: Exception) { false }
    }

    override fun getChatMessages(eventId: String): Flow<List<ChatMessage>> {
        return eventsCollection.document(eventId).collection("chat").snapshots.map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                try { doc.data<ChatMessage>().copy(id = doc.id) } catch (e: Exception) { null }
            }.sortedBy { it.timestamp }
        }
    }

    override suspend fun sendChatMessage(eventId: String, message: ChatMessage): Boolean {
        return try {
            eventsCollection.document(eventId).collection("chat").add(message)
            true
        } catch (e: Exception) { false }
    }

    // --- 6. PERFIL DE UTILIZADOR & FAVORITOS ---

    override suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val doc = usersCollection.document(userId).get()
            if (doc.exists) doc.data<UserProfile>() else null
        } catch (e: Exception) { null }
    }

    override suspend fun updateUserProfile(userId: String, profile: UserProfile): Boolean {
        return try {
            usersCollection.document(userId).set(profile, merge = true)
            true
        } catch (e: Exception) { false }
    }

    override suspend fun updateUserFcmToken(userId: String, token: String) {
        try { usersCollection.document(userId).update("fcmToken" to token) } catch (e: Exception) { }
    }

    override fun getFavoriteEventIds(userId: String): Flow<List<String>> {
        if (userId.isBlank()) return flowOf(emptyList())
        return usersCollection.document(userId).collection("favorites").snapshots
            .map { snapshot -> snapshot.documents.map { it.id } }
            .catch { emit(emptyList()) }
    }

    override suspend fun toggleFavorite(userId: String, eventId: String) {
        try {
            val favDoc = usersCollection.document(userId).collection("favorites").document(eventId)
            val snapshot = favDoc.get()
            if (snapshot.exists) favDoc.delete() else favDoc.set(mapOf("savedAt" to Clock.System.now().toEpochMilliseconds()))
        } catch (e: Exception) { }
    }

    override fun isFollowing(userId: String, organizerId: String): Flow<Boolean> {
        if (userId.isBlank() || organizerId.isBlank()) return flowOf(false)
        return usersCollection.document(userId).collection("following").document(organizerId)
            .snapshots.map { it.exists }.catch { emit(false) }
    }

    override suspend fun toggleFollow(userId: String, organizerId: String) {
        try {
            val followDoc = usersCollection.document(userId).collection("following").document(organizerId)
            if (followDoc.get().exists) followDoc.delete()
            else followDoc.set(mapOf("followedAt" to Clock.System.now().toEpochMilliseconds()))
        } catch (e: Exception) { }
    }

    // --- 7. NOTIFICAÇÕES ---

    override fun getUserNotifications(userId: String): Flow<List<NotificationItem>> {
        return usersCollection.document(userId).collection("notifications").snapshots.map { snapshot ->
            snapshot.documents.mapNotNull { doc ->
                try { doc.data<NotificationItem>().copy(id = doc.id) } catch (e: Exception) { null }
            }.sortedByDescending { it.timestamp }
        }
    }

    override suspend fun markNotificationAsRead(userId: String, notificationId: String) {
        try {
            usersCollection.document(userId).collection("notifications").document(notificationId)
                .update("isRead" to true)
        } catch (e: Exception) { }
    }

    override suspend fun createTestNotification(userId: String) {
        val fakeNotification = NotificationItem(
            title = "Bem-vindo ao Eventify!",
            message = "Esta é a tua primeira notificação. Compra bilhetes agora!",
            timestamp = Clock.System.now().toEpochMilliseconds(),
            isRead = false,
            type = "success"
        )
        usersCollection.document(userId).collection("notifications").add(fakeNotification)
    }

    // --- 8. AUXILIARES ---

    override fun hasTicketFlow(userId: String, eventId: String): Flow<Boolean> {
        return ticketsCollection.where { "userId" equalTo userId }.where { "eventId" equalTo eventId }
            .snapshots.map { !it.documents.isEmpty() }.catch { emit(false) }
    }

    override suspend fun getUserTicketCount(userId: String): Int {
        return try { ticketsCollection.where { "userId" equalTo userId }.get().documents.size } catch (e: Exception) { 0 }
    }

    override suspend fun getUserCommentCount(userId: String): Int {
        return try {
            firestore.collectionGroup("comments").where { "userId" equalTo userId }.get().documents.size
        } catch (e: Exception) { 0 }
    }
}