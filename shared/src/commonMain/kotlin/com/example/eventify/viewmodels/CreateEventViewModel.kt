package com.example.eventify.viewmodels

import com.example.eventify.model.Category
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepositoryKMM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CreateEventViewModel(
    private val repository: EventRepositoryKMM,
    private val organizerId: String
) {
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun createEvent(
        id: String,
        title: String,
        description: String,
        location: String,
        imageUrl: String,
        dateTime: String,
        category: Category
    ) {
        val event = Event(
            id = id,
            title = title,
            description = description,
            location = location,
            imageUrl = imageUrl,
            dateTime = dateTime,
            category = category,
            isRegistered = true // ou false dependendo se o criador está automaticamente registado
        )

        viewModelScope.launch {
            repository.addEvent(event)
        }
    }
}
