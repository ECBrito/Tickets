package com.example.eventify.data

import com.example.eventify.model.Event
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.InternalSerializationApi

private const val EVENTS_COLLECTION = "events"

class FirestoreEventService(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {

    // Limites
    private val MAX_NAME_LENGTH = 100
    private val MAX_DESCRIPTION_LENGTH = 2000

    private fun validateEvent(event: Event) {
        require(event.title.isNotBlank()) { "O título não pode estar vazio" }
        require(event.title.length <= MAX_NAME_LENGTH) { "Título demasiado longo" }
        require(event.description.length <= MAX_DESCRIPTION_LENGTH) { "Descrição excede o limite" }
        // Sanitização básica: remover espaços extras
        event.copy(
            title = event.title.trim(),
            description = event.description.trim()
        )
    }
    @OptIn(InternalSerializationApi::class)
    fun getAllEventsFlow(): Flow<List<Event>> {
        return firestore.collection(EVENTS_COLLECTION)
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { document ->
                    document.data<Event>()?.copy(id = document.id)
                }
            }
    }
    @OptIn(InternalSerializationApi::class)
    suspend fun saveEvent(event: Event) {
        validateEvent(event)
        val collectionRef = firestore.collection(EVENTS_COLLECTION)
        if (event.id.isEmpty()) {
            collectionRef.add(event)
        } else {
            collectionRef.document(event.id).set(event)
        }
    }
    suspend fun deleteEvent(eventId: String) {
        firestore.collection(EVENTS_COLLECTION).document(eventId).delete()
    }
}
