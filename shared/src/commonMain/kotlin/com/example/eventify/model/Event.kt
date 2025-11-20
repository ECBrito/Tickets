package com.example.eventify.model // Certifica-te que o package é este

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Event(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String,
    @SerialName("description") val description: String,
    @SerialName("date") val date: String,
    @SerialName("location") val location: String,
    @SerialName("imageUrl") val imageUrl: String? = null,

    @SerialName("category")
    val category: EventCategory = EventCategory.OTHER, // Usa o enum partilhado

    // Campos adicionais que podem ser úteis
    @SerialName("price") val price: Double = 0.0,
    @SerialName("currency") val currency: String = "USD",
    @SerialName("isSaved") val isSaved: Boolean = false,
    @SerialName("isRegistered") val isRegistered: Boolean = false,
    @SerialName("organizer") val organizer: String = ""
)