package com.example.eventify.repository

import com.example.eventify.model.Comment
import com.example.eventify.model.Event
import com.example.eventify.model.Ticket
import com.example.eventify.model.TicketValidationResult
import com.example.eventify.model.UserProfile
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.serialization.InternalSerializationApi // Import necessário

@OptIn(InternalSerializationApi::class) // Corrige os erros de "Needs opt-in"
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
                val event = doc.data<Event>()
                event.copy(id = doc.id)
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
            println("Erro na query de utilizador: ${e.message}")
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
            } catch (e: Exception) {
                mutableListOf<String>()
            }

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
        } catch (e: Exception) {
            println("Erro no upload: ${e.message}")
            null
        }
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
        } catch (e: Exception) {
            println("Erro ao comprar bilhetes: ${e.message}")
            false
        }
    }

    // --- MEUS BILHETES ---
    override suspend fun getUserTickets(userId: String): List<Ticket> {
        return try {
            // CORREÇÃO DA QUERY: Sintaxe lambda correta
            val snapshot = ticketsCollection.where { "userId" equalTo userId }.get()

            snapshot.documents.mapNotNull { doc ->
                try {
                    val ticket = doc.data<Ticket>()
                    ticket.copy(id = doc.id)
                } catch (e: Exception) {
                    println("Erro ao ler bilhete: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Erro fatal ao buscar tickets: ${e.message}")
            emptyList()
        }
    }

    // --- VALIDAÇÃO DE BILHETES ---
    override suspend fun validateTicket(ticketId: String): TicketValidationResult {
        return try {
            val docRef = ticketsCollection.document(ticketId)
            val snapshot = docRef.get()

            if (!snapshot.exists) {
                return TicketValidationResult.INVALID
            }

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
            val commentsRef = eventsCollection.document(eventId).collection("comments")
            commentsRef.add(comment)
            true
        } catch (e: Exception) {
            false
        }
    }

    // --- PERFIL DO UTILIZADOR ---
    override suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val doc = usersCollection.document(userId).get()
            if (doc.exists) {
                doc.data<UserProfile>()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateUserProfile(userId: String, profile: UserProfile): Boolean {
        return try {
            usersCollection.document(userId).set(profile, merge = true)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun uploadProfileImage(imageBytes: ByteArray, userId: String): String? {
        return try {
            val storageRef = storage.reference.child("profile_images/$userId.jpg")
            val dataObj = Data(imageBytes)
            storageRef.putData(dataObj)
            storageRef.getDownloadUrl()
        } catch (e: Exception) {
            null
        }
    }

    override fun getFavoriteEventIds(userId: String): Flow<List<String>> {
        // Escuta a coleção: users/{userId}/favorites
        return usersCollection.document(userId).collection("favorites").snapshots.map { snapshot ->
            snapshot.documents.map { it.id } // Retorna apenas os IDs dos eventos
        }
    }

    override suspend fun toggleFavorite(userId: String, eventId: String) {
        try {
            val favDoc = usersCollection.document(userId).collection("favorites").document(eventId)
            val snapshot = favDoc.get()

            if (snapshot.exists) {
                // Se já existe, remove (Desmarcar)
                favDoc.delete()
            } else {
                // Se não existe, cria (Marcar) - Guardamos timestamp
                val data = mapOf("savedAt" to Clock.System.now().toEpochMilliseconds())
                favDoc.set(data)
            }
        } catch (e: Exception) {
            println("Erro ao mudar favorito: ${e.message}")
        }
    }
}