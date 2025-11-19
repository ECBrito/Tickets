package com.example.eventify.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.eventify.model.Event
import com.example.eventify.model.EventCategory
import com.example.eventify.repository.EventRepository
import kotlinx.datetime.LocalDateTime
import java.util.UUID

class CreateEventViewModel : ViewModel() {

    private val repository = EventRepository

    fun createEvent(
        title: String,
        description: String,
        categoryName: String,
        location: String,
        dateStr: String,
        imageUri: String?, // <--- NOVO PARÂMETRO (Pode ser nulo se o user não escolher foto)
        onSuccess: () -> Unit
    ) {
        // Lógica para determinar a imagem final
        // Se o utilizador escolheu uma foto, usamos essa URI.
        // Se não, usamos uma imagem de placeholder aleatória do Unsplash.
        val finalImageUrl = if (imageUri.isNullOrBlank()) {
            "https://images.unsplash.com/photo-1492684223066-81342ee5ff30?w=500"
        } else {
            imageUri
        }

        val newEvent = Event(
            id = UUID.randomUUID().toString(),
            title = title,
            category = EventCategory.entries.find { it.name == categoryName } ?: EventCategory.OTHER,
            location = location,
            dateTime = LocalDateTime(2025, 1, 1, 12, 0), // Placeholder de data (já tens a lógica na UI)
            imageUrl = finalImageUrl, // <--- USAR A IMAGEM ESCOLHIDA
            price = 0.0,
            currency = "USD",
            isSaved = false,
            isRegistered = false,
            organizer = "Me (Organizer)"
        )

        repository.addEvent(newEvent)
        onSuccess()
    }
}