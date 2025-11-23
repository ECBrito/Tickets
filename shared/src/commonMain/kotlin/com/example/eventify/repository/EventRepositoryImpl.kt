package com.example.eventify.repository

import com.example.eventify.model.Event
import com.example.eventify.model.Ticket
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.where
import dev.gitlive.firebase.storage.Data
import dev.gitlive.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import com.example.eventify.model.TicketValidationResult
import kotlinx.serialization.InternalSerializationApi

class EventRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : EventRepository {

    private val eventsCollection = firestore.collection("events")
    private val ticketsCollection = firestore.collection("tickets")

    // --- LEITURA EM TEMPO REAL (FLOW) ---
    override val events: Flow<List<Event>> = eventsCollection.snapshots.map { snapshot ->
        snapshot.documents.mapNotNull { doc ->
            try {
                Event(
                    id = doc.id,
                    title = doc.get<String>("title"),
                    description = doc.get<String>("description"),
                    location = doc.get<String>("location"),
                    imageUrl = doc.get<String>("imageUrl"),
                    dateTime = doc.get<String>("dateTime"),
                    category = doc.get<String>("category"),
                    registeredUserIds = try {
                        doc.get<List<String>>("registeredUserIds")
                    } catch (e: Exception) {
                        emptyList()
                    },
                    price = try { doc.get<Double>("price") } catch (e: Exception) { 0.0 }
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
            val eventData = mapOf(
                "title" to event.title,
                "description" to event.description,
                "location" to event.location,
                "imageUrl" to event.imageUrl,
                "dateTime" to event.dateTime,
                "category" to event.category,
                "registeredUserIds" to event.registeredUserIds,
                "price" to event.price
            )

            if (event.id.isNotEmpty()) {
                eventsCollection.document(event.id).update(eventData)
            } else {
                eventsCollection.add(eventData)
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
                    Event(
                        id = doc.id,
                        title = doc.get<String>("title"),
                        description = doc.get<String>("description"),
                        location = doc.get<String>("location"),
                        imageUrl = doc.get<String>("imageUrl"),
                        dateTime = doc.get<String>("dateTime"),
                        category = doc.get<String>("category"),
                        registeredUserIds = try { doc.get<List<String>>("registeredUserIds") } catch(e:Exception) { emptyList() },
                        isRegistered = true,
                        price = try { doc.get<Double>("price") } catch (e: Exception) { 0.0 }
                    )
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

    // --- PESQUISA LOCAL ---
    override fun searchEvents(query: String, currentList: List<Event>): List<Event> {
        if (query.isBlank()) return currentList
        return currentList.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.location.contains(query, ignoreCase = true)
        }
    }

    // --- COMPRA DE BILHETES ---
    @OptIn(InternalSerializationApi::class)
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

    // --- MEUS BILHETES (CORRIGIDO) ---
    @OptIn(InternalSerializationApi::class)
    override suspend fun getUserTickets(userId: String): List<Ticket> {
        return try {
            // Vai buscar os documentos onde userId é igual ao utilizador atual
            val snapshot = ticketsCollection.where("userId", userId).get()

            snapshot.documents.mapNotNull { doc ->
                try {
                    // LEITURA MANUAL SEGURA
                    Ticket(
                        id = doc.id,
                        userId = doc.get<String>("userId"),
                        eventId = doc.get<String>("eventId"),
                        eventTitle = doc.get<String>("eventTitle"),
                        eventLocation = doc.get<String>("eventLocation"),
                        eventDate = doc.get<String>("eventDate"),
                        eventImage = doc.get<String>("eventImage"),

                        // CORREÇÃO DE SEGURANÇA:
                        // Lemos como Double (formato nativo do JSON/Firestore) e convertemos para Long
                        purchaseDate = try {
                            doc.get<Double>("purchaseDate").toLong()
                        } catch (e: Exception) {
                            0L
                        },

                        isValid = doc.get<Boolean>("isValid")
                    )
                } catch (e: Exception) {
                    println("Erro ao ler bilhete individual: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Erro fatal ao buscar tickets: ${e.message}")
            emptyList()
        }
    }
    override suspend fun validateTicket(ticketId: String): TicketValidationResult {
        return try {
            val docRef = ticketsCollection.document(ticketId)
            val snapshot = docRef.get()

            if (!snapshot.exists) {
                return TicketValidationResult.INVALID
            }

            // Verifica se o campo 'isValid' é verdadeiro
            val isValid = snapshot.get<Boolean>("isValid")

            if (isValid) {
                // SUCESSO: Marca como usado na BD para não entrar 2x
                docRef.update("isValid" to false)
                TicketValidationResult.VALID
            } else {
                TicketValidationResult.ALREADY_USED
            }
        } catch (e: Exception) {
            println("Erro a validar: ${e.message}")
            TicketValidationResult.ERROR
        }
    }
}