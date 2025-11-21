package com.example.eventify.data

import com.example.eventify.model.Event
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val EVENTS_COLLECTION = "events"

class FirestoreEventService(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {

    fun getAllEventsFlow(): Flow<List<Event>> {
        return firestore.collection(EVENTS_COLLECTION)
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { document ->
                    document.data<Event>()?.copy(id = document.id)
                }
            }
    }

    suspend fun saveEvent(event: Event) {
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
