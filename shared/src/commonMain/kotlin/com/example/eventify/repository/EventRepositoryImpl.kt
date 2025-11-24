package com.example.eventify.repository

import com.example.eventify.model.Attendee // <--- O IMPORT QUE FALTAVA
import com.example.eventify.model.Comment
import com.example.eventify.model.Event
import com.example.eventify.model.Ticket
import com.example.eventify.model.TicketValidationResult
import com.example.eventify.model.UserProfile
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

    // --- LEITURA EM TEMPO REAL ---
    override val events: Flow<List<Event>> = eventsCollection.snapshots.map { snapshot ->
        snapshot.documents.mapNotNull { doc ->
            try {
                // Tenta ler manualmente se o automático falhar por causa de tipos misturados
                val title = try { doc.get<String>("title") } catch (e: Exception) { "" }
                val description = try { doc.get<String>("description") } catch (e: Exception) { "" }
                val location = try { doc.get<String>("location") } catch (e: Exception) { "" }
                val imageUrl = try { doc.get<String>("imageUrl") } catch (e: Exception) { "" }
                val dateTime = try { doc.get<String>("dateTime") } catch (e: Exception) { "" }
                val category = try { doc.get<String>("category") } catch (e: Exception) { "Other" }

                val registeredUserIds = try {
                    doc.get<List<String>>("registeredUserIds")
                } catch (e: Exception) { emptyList() }

                val price = try {
                    doc.get<Double>("price")
                } catch (e: Exception) {
                    try { doc.get<Int>("price").toDouble() } catch(e2: Exception) { 0.0 }
                }

                Event(
                    id = doc.id,
                    title = title,
                    description = description,
                    location = location,
                    imageUrl = imageUrl,
                    dateTime = dateTime,
                    category = category,
                    registeredUserIds = registeredUserIds,
                    price = price,
                    isRegistered = false,
                    isSaved = false
                )
            } catch (e: Exception) {
                println("Erro ao ler evento: ${e.message}")
                null
            }
        }
    }

    // --- ADICIONAR EVENTO ---
    override suspend fun addEvent(event: Event): Boolean {
        return try {
            if (event.id.isNotEmpty()) {
                eventsCollection.document(event.id).set(event, merge = true)
            } else {
                eventsCollection.add(event)
            }
            true
        } catch (e: Exception) {
            println("Erro ao adicionar: ${e.message}")
            false
        }
    }

    // --- APAGAR EVENTO ---
    override suspend fun deleteEvent(eventId: String): Boolean {
        return try {
            eventsCollection.document(eventId).delete()
            true
        } catch (e: Exception) {
            println("Erro ao apagar: ${e.message}")
            false
        }
    }

    // --- BUSCAR REGISTADOS ---
    override suspend fun getEventsRegisteredByUser(userId: String): List<Event> {
        return try {
            val querySnapshot = eventsCollection
                .where { "registeredUserIds" contains userId }
                .get()

            querySnapshot.documents.mapNotNull { doc ->
                try {
                    val event = doc.data<Event>()
                    event.copy(id = doc.id, isRegistered = true)
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- TOGGLE RSVP ---
    override suspend fun toggleEventRegistration(eventId: String, userId: String) {
        try {
            val docRef = eventsCollection.document(eventId)
            val snapshot = docRef.get()
            val currentUsers = try {
                snapshot.get<List<String>>("registeredUserIds").toMutableList()
            } catch (e: Exception) { mutableListOf() }

            if (currentUsers.contains(userId)) {
                currentUsers.remove(userId)
            } else {
                currentUsers.add(userId)
            }
            docRef.update("registeredUserIds" to currentUsers)
        } catch (e: Exception) {
            println("Erro ao atualizar registo: ${e.message}")
        }
    }

    // --- UPLOAD DE IMAGEM ---
    override suspend fun uploadEventImage(imageBytes: ByteArray, fileName: String): String? {
        return try {
            val storageRef = storage.reference.child("event_images/$fileName")
            val dataObj = Data(imageBytes)
            storageRef.putData(dataObj)
            storageRef.getDownloadUrl()
        } catch (e: Exception) { null }
    }

    // --- PESQUISA ---
    override fun searchEvents(query: String, currentList: List<Event>): List<Event> {
        if (query.isBlank()) return currentList
        return currentList.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.location.contains(query, ignoreCase = true)
        }
    }

    // --- COMPRA DE BILHETES ---
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

    // --- MEUS BILHETES ---
    override suspend fun getUserTickets(userId: String): List<Ticket> {
        return try {
            val snapshot = ticketsCollection.where { "userId" equalTo userId }.get()
            snapshot.documents.mapNotNull { doc ->
                try {
                    Ticket(
                        id = doc.id,
                        userId = doc.get("userId"),
                        eventId = doc.get("eventId"),
                        eventTitle = doc.get("eventTitle"),
                        eventLocation = doc.get("eventLocation"),
                        eventDate = doc.get("eventDate"),
                        eventImage = doc.get("eventImage"),
                        purchaseDate = try { doc.get<Double>("purchaseDate").toLong() } catch(e:Exception) { 0L },
                        isValid = doc.get("isValid")
                    )
                } catch (e: Exception) { null }
            }
        } catch (e: Exception) { emptyList() }
    }

    // --- TRANSFERÊNCIA ---
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

    // --- VALIDAÇÃO ---
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
        } catch (e: Exception) {
            TicketValidationResult.ERROR
        }
    }

    // --- LISTA DE PARTICIPANTES (ATTENDEES) ---
    override suspend fun getEventAttendees(eventId: String): List<Attendee> {
        return try {
            // 1. Buscar todos os bilhetes deste evento
            val ticketsSnapshot = ticketsCollection.where { "eventId" equalTo eventId }.get()

            // Mapeia manualmente para evitar erros de serialização
            val tickets = ticketsSnapshot.documents.mapNotNull { doc ->
                try {
                    Ticket(
                        id = doc.id,
                        userId = doc.get("userId"),
                        isValid = doc.get("isValid")
                    )
                } catch (e:Exception) { null }
            }

            // 2. Para cada bilhete, buscar os dados do utilizador
            tickets.mapNotNull { ticket ->
                val userProfile = getUserProfile(ticket.userId)

                if (userProfile != null) {
                    Attendee(
                        ticketId = ticket.id,
                        userId = userProfile.id,
                        name = if (userProfile.name.isNotBlank()) userProfile.name else "User",
                        email = userProfile.email,
                        photoUrl = userProfile.photoUrl,
                        isCheckedIn = !ticket.isValid // Se isValid=false, Check-in feito
                    )
                } else {
                    // Fallback se user não tiver perfil criado
                    Attendee(
                        ticketId = ticket.id,
                        userId = ticket.userId,
                        name = "Guest User",
                        email = "No email",
                        photoUrl = "",
                        isCheckedIn = !ticket.isValid
                    )
                }
            }
        } catch (e: Exception) {
            println("Erro ao buscar attendees: ${e.message}")
            emptyList()
        }
    }

    override suspend fun manualCheckIn(ticketId: String): Boolean {
        return try {
            ticketsCollection.document(ticketId).update("isValid" to false)
            true
        } catch (e: Exception) { false }
    }

    // --- COMENTÁRIOS ---
    override fun getComments(eventId: String): Flow<List<Comment>> {
        return eventsCollection.document(eventId).collection("comments")
            .snapshots
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.data<Comment>().copy(id = doc.id)
                    } catch (e: Exception) { null }
                }.sortedByDescending { it.timestamp }
            }
    }

    override suspend fun addComment(eventId: String, comment: Comment): Boolean {
        return try {
            eventsCollection.document(eventId).collection("comments").add(comment)
            true
        } catch (e: Exception) { false }
    }

    // --- PERFIL ---
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
        try {
            usersCollection.document(userId).update("fcmToken" to token)
        } catch (e: Exception) { }
    }

    // --- FAVORITOS ---
    override fun getFavoriteEventIds(userId: String): Flow<List<String>> {
        if (userId.isBlank()) return flowOf(emptyList())
        return usersCollection.document(userId).collection("favorites")
            .snapshots
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
}