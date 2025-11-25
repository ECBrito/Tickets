package com.example.eventify.repository

import com.example.eventify.model.Attendee
import com.example.eventify.model.Comment
import com.example.eventify.model.Event
import com.example.eventify.model.NotificationItem
import com.example.eventify.model.Ticket
import com.example.eventify.model.TicketValidationResult
import com.example.eventify.model.UserProfile
import com.example.eventify.model.PromoCode
import com.example.eventify.model.Review
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

    // --- EVENTOS ---
    override val events: Flow<List<Event>> = eventsCollection.snapshots.map { snapshot ->
        snapshot.documents.mapNotNull { doc ->
            try {
                val event = doc.data<Event>()
                event.copy(id = doc.id)
            } catch (e: Exception) { null }
        }
    }

    override suspend fun addEvent(event: Event): Boolean {
        return try {
            if (event.id.isNotEmpty()) eventsCollection.document(event.id).set(event, merge = true)
            else eventsCollection.add(event)
            true
        } catch (e: Exception) { false }
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
                try { doc.data<Event>().copy(id = doc.id, isRegistered = true) } catch (e: Exception) { null }
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun toggleEventRegistration(eventId: String, userId: String) {
        try {
            val docRef = eventsCollection.document(eventId)
            val snapshot = docRef.get()
            val currentUsers = try { snapshot.get<List<String>>("registeredUserIds").toMutableList() } catch (e: Exception) { mutableListOf() }
            if (currentUsers.contains(userId)) currentUsers.remove(userId) else currentUsers.add(userId)
            docRef.update("registeredUserIds" to currentUsers)
        } catch (e: Exception) { }
    }

    override fun searchEvents(query: String, currentList: List<Event>): List<Event> {
        if (query.isBlank()) return currentList
        return currentList.filter {
            it.title.contains(query, ignoreCase = true) || it.location.contains(query, ignoreCase = true)
        }
    }

    override suspend fun incrementEventShares(eventId: String) {
        try {
            val docRef = eventsCollection.document(eventId)
            val event = docRef.get().data<Event>()
            docRef.update("shares" to event.shares + 1)
        } catch (e: Exception) { }
    }

    // --- IMAGENS ---
    override suspend fun uploadEventImage(imageBytes: ByteArray, fileName: String): String? {
        return try {
            val storageRef = storage.reference.child("event_images/$fileName")
            val dataObj = Data(imageBytes)
            storageRef.putData(dataObj)
            storageRef.getDownloadUrl()
        } catch (e: Exception) { null }
    }

    // --- BILHETES & COMPRA ---
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
                    eventLocation = event.location,
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
            snapshot.documents.mapNotNull { doc -> try { doc.data<Ticket>().copy(id = doc.id) } catch (e: Exception) { null } }
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

    // --- GESTÃO DE PARTICIPANTES (ORGANIZADOR & PÚBLICO) ---
    override suspend fun getEventAttendees(eventId: String): List<Attendee> {
        return try {
            val ticketsSnapshot = ticketsCollection.where { "eventId" equalTo eventId }.get()
            val tickets = ticketsSnapshot.documents.mapNotNull { doc ->
                try {
                    Ticket(id = doc.id, userId = doc.get("userId"), isValid = doc.get("isValid"))
                } catch (e:Exception) { null }
            }

            tickets.mapNotNull { ticket ->
                val userProfile = getUserProfile(ticket.userId)
                if (userProfile != null) {
                    Attendee(
                        ticketId = ticket.id,
                        userId = userProfile.id,
                        name = if (userProfile.name.isNotBlank()) userProfile.name else "User",
                        email = userProfile.email,
                        photoUrl = userProfile.photoUrl,
                        isCheckedIn = !ticket.isValid,
                        isPublic = userProfile.isPublic // Agora isto não dá erro
                    )
                } else {
                    Attendee(ticket.id, ticket.userId, "Guest", "", "", !ticket.isValid, false)
                }
            }
        } catch (e: Exception) { emptyList() }
    }

    override suspend fun getPublicAttendeesPreview(eventId: String): List<Attendee> {
        val allAttendees = getEventAttendees(eventId)
        // Filtra apenas quem tem perfil público e foto
        return allAttendees.filter { it.isPublic && it.photoUrl.isNotBlank() }
    }

    override suspend fun manualCheckIn(ticketId: String): Boolean {
        return try {
            ticketsCollection.document(ticketId).update("isValid" to false)
            true
        } catch (e: Exception) { false }
    }

    // --- COMENTÁRIOS ---
    override fun getComments(eventId: String): Flow<List<Comment>> {
        return eventsCollection.document(eventId).collection("comments").snapshots.map { snapshot ->
            snapshot.documents.mapNotNull { doc -> try { doc.data<Comment>().copy(id = doc.id) } catch (e: Exception) { null } }.sortedByDescending { it.timestamp }
        }
    }

    override suspend fun addComment(eventId: String, comment: Comment): Boolean {
        return try {
            eventsCollection.document(eventId).collection("comments").add(comment)
            true
        } catch (e: Exception) { false }
    }

    // --- PERFIL & USER ---
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

    override suspend fun uploadProfileImage(imageBytes: ByteArray, userId: String): String? {
        return try {
            val storageRef = storage.reference.child("profile_images/$userId.jpg")
            val dataObj = Data(imageBytes)
            storageRef.putData(dataObj)
            storageRef.getDownloadUrl()
        } catch (e: Exception) { null }
    }

    override suspend fun updateUserFcmToken(userId: String, token: String) {
        try { usersCollection.document(userId).update("fcmToken" to token) } catch (e: Exception) { }
    }

    // --- FAVORITOS ---
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

        // Ouve o documento específico: users/{eu}/following/{organizerId}
        return usersCollection.document(userId).collection("following").document(organizerId)
            .snapshots
            .map { it.exists } // Se o documento existe, é porque sigo
            .catch { emit(false) }
    }

    override suspend fun toggleFollow(userId: String, organizerId: String) {
        if (userId.isBlank() || organizerId.isBlank()) return

        try {
            val followDoc = usersCollection.document(userId).collection("following").document(organizerId)
            val snapshot = followDoc.get()

            if (snapshot.exists) {
                followDoc.delete() // Deixar de seguir
            } else {
                // Seguir (Guardamos a data)
                followDoc.set(mapOf("followedAt" to Clock.System.now().toEpochMilliseconds()))
            }
        } catch (e: Exception) {
            println("Erro no follow: ${e.message}")
        }
    }

    // --- NOTIFICAÇÕES ---
    override fun getUserNotifications(userId: String): Flow<List<NotificationItem>> {
        return usersCollection.document(userId).collection("notifications")
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data<NotificationItem>().copy(id = doc.id)
                    } catch (e: Exception) { null }
                }.sortedByDescending { it.timestamp }
            }
    }

    override suspend fun markNotificationAsRead(userId: String, notificationId: String) {
        try {
            usersCollection.document(userId)
                .collection("notifications")
                .document(notificationId)
                .update("isRead" to true)
        } catch (e: Exception) { }
    }

    // Função auxiliar para criares dados de teste
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

    // --- REVIEWS ---
    override fun getReviews(eventId: String): Flow<List<Review>> {
        return eventsCollection.document(eventId).collection("reviews")
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try { doc.data<Review>().copy(id = doc.id) } catch (e: Exception) { null }
                }.sortedByDescending { it.timestamp }
            }
    }

    override suspend fun addReview(eventId: String, review: Review): Boolean {
        return try {
            val eventRef = eventsCollection.document(eventId)

            // 1. Guardar a Review na sub-coleção
            eventRef.collection("reviews").add(review)

            // 2. Atualizar a média do Evento (Leitura + Escrita)
            // Nota: Numa app real faríamos isto numa Cloud Function ou Transação para segurança.
            // Aqui fazemos no cliente para simplicidade do MVP.
            val eventSnapshot = eventRef.get()
            val currentRating = try { eventSnapshot.get<Double>("rating") } catch(e:Exception) { 0.0 }
            val currentCount = try { eventSnapshot.get<Int>("reviewCount") } catch(e:Exception) { 0 }

            val newCount = currentCount + 1
            val newRating = ((currentRating * currentCount) + review.rating) / newCount

            eventRef.update(
                "rating" to newRating,
                "reviewCount" to newCount
            )
            true
        } catch (e: Exception) {
            println("Erro review: ${e.message}")
            false
        }
    }
}