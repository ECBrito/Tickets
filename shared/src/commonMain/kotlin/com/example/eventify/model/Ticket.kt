package com.example.eventify.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

@InternalSerializationApi @Serializable
data class Ticket(
    val id: String = "",
    val userId: String = "",
    val eventId: String = "",
    val eventTitle: String = "",     // Guardar redundante para facilitar listagem
    val eventLocation: String = "",  // Guardar redundante
    val eventDate: String = "",      // Guardar redundante
    val eventImage: String = "",     // Guardar redundante
    val purchaseDate: Long = 0,
    val isValid: Boolean = true
)

