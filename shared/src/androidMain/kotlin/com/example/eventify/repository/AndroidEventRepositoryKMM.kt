package com.example.eventify.repository

import com.example.eventify.model.Category
import com.example.eventify.model.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class AndroidEventRepositoryKMM : EventRepositoryKMM {

    private val firestore = FirebaseFirestore.getInstance()
    private val eventsCollection = firestore.collection("events")
    private var listener: ListenerRegistration? = null

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    override val events: StateFlow<List<Event>> = _events

    override suspend fun startListening() {
        listener = eventsCollection.addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener

            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(EventFirebase::class.java)?.toEvent()
            }
            _events.value = list
        }
    }

    override fun stopListening() {
        listener?.remove()
        listener = null
    }

    override suspend fun addEvent(event: Event): Boolean {
        return try {
            eventsCollection.document(event.id).set(EventFirebase.fromEvent(event)).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteEvent(eventId: String): Boolean {
        return try {
            eventsCollection.document(eventId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override fun getEventById(id: String): Event? = _events.value.find { it.id == id }

    override fun searchEvents(query: String): List<Event> =
        _events.value.filter { it.title.contains(query, ignoreCase = true) }

    override suspend fun getEventsRegisteredByUser(userId: String): List<Event> =
        _events.value.filter { it.registeredUserIds.contains(userId) }

    override suspend fun toggleEventRegistration(eventId: String, userId: String) {
        val event = getEventById(eventId) ?: return
        val updatedUsers = if (event.registeredUserIds.contains(userId)) {
            event.registeredUserIds - userId
        } else {
            event.registeredUserIds + userId
        }
        eventsCollection.document(eventId).update("registeredUserIds", updatedUsers).await()
    }

    // Classe auxiliar para mapear Firestore
    private data class EventFirebase(
        val id: String = "",
        val title: String = "",
        val description: String = "",
        val location: String = "",
        val imageUrl: String = "",
        val dateTime: String = "",
        val category: String = "",
        val registeredUserIds: List<String> = emptyList()
    ) {
        fun toEvent(): Event = Event(
            id = id,
            title = title,
            description = description,
            location = location,
            imageUrl = imageUrl,
            dateTime = dateTime,
            category = Category.valueOf(category),
            registeredUserIds = registeredUserIds,
            isRegistered = false
        )

        companion object {
            fun fromEvent(event: Event) = EventFirebase(
                id = event.id,
                title = event.title,
                description = event.description,
                location = event.location,
                imageUrl = event.imageUrl,
                dateTime = event.dateTime,
                category = event.category.name,
                registeredUserIds = event.registeredUserIds
            )
        }
    }
}
