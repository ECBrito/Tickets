package com.example.eventify.repository

import com.example.eventify.model.Event
import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EventRepositoryImpl(
    private val firestore: FirebaseFirestore
) : EventRepository {

    private val eventsCollection = firestore.collection("events")

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
                    registeredUserIds = try { doc.get<List<String>>("registeredUserIds") } catch (e: Exception) { emptyList() }
                )
            } catch (e: Exception) {
                println("Erro parse: ${e.message}")
                null
            }
        }
    }

    override suspend fun addEvent(event: Event): Boolean {
        return try {
            val eventData = mapOf(
                "title" to event.title,
                "description" to event.description,
                "location" to event.location,
                "imageUrl" to event.imageUrl,
                "dateTime" to event.dateTime,
                "category" to event.category,
                "registeredUserIds" to event.registeredUserIds
            )
            if (event.id.isNotEmpty()) eventsCollection.document(event.id).update(eventData)
            else eventsCollection.add(eventData)
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
            val snapshot = eventsCollection.where { "registeredUserIds" contains userId }.get()
            snapshot.documents.mapNotNull { doc ->
                try {
                    Event(
                        id = doc.id,
                        title = doc.get("title"),
                        location = doc.get("location"),
                        category = doc.get("category"), // Adicionei campos mínimos necessários
                        isRegistered = true
                    )
                } catch (e: Exception) { null }
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
        } catch (e: Exception) { println("Erro toggle: ${e.message}") }
    }

    override fun searchEvents(query: String, currentList: List<Event>): List<Event> {
        if (query.isBlank()) return currentList
        return currentList.filter { it.title.contains(query, ignoreCase = true) }
    }
}