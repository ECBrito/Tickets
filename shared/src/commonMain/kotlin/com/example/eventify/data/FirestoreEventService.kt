package com.example.eventify.data

import com.example.eventify.model.Event
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
// Nota: Se 'await' não for encontrado, remove este import.
// A biblioteca gitlive usa funções suspensas nativas, o 'await' pode ser desnecessário.

// Constante para o nome da coleção no Firestore
private const val EVENTS_COLLECTION = "events"

class FirestoreEventService(
    private val firestore: FirebaseFirestore = Firebase.firestore
) {

    // --- Operações CRUD (Create, Read, Update, Delete) ---

    /**
     * Obtém todos os eventos do Firestore em tempo real (Flow).
     */
    fun getAllEventsFlow(): Flow<List<Event>> {
        return firestore.collection(EVENTS_COLLECTION)
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents.map { document ->
                    // O Firebase KMP converte automaticamente o JSON para o objeto Event
                    // A função data() é reified inline, por isso inferimos o tipo <Event>
                    val event = document.data<Event>()
                    // Garantimos que o ID do objeto é igual ao ID do documento
                    event.copy(id = document.id)
                }
            }
    }

    /**
     * Guarda ou atualiza um evento.
     */
    suspend fun saveEvent(event: Event) {
        val collectionRef = firestore.collection(EVENTS_COLLECTION)

        if (event.id.isEmpty()) {
            // Criar novo: O Firestore gera o ID
            // CORREÇÃO: Chamada simplificada passando o objeto diretamente
            collectionRef.add(event)
        } else {
            // Atualizar existente: Usamos o ID para sobrescrever
            // CORREÇÃO: Chamada simplificada passando o objeto diretamente
            collectionRef.document(event.id).set(event)
        }
    }

    /**
     * Elimina um evento.
     */
    suspend fun deleteEvent(eventId: String) {
        firestore.collection(EVENTS_COLLECTION).document(eventId).delete()
    }
}