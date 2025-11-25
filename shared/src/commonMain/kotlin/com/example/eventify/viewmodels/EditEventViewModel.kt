package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.InternalSerializationApi

class EditEventViewModel(
    private val repository: EventRepository,
    private val eventId: String
) : ViewModel() {

    @OptIn(InternalSerializationApi::class)
    private val _event = MutableStateFlow<Event?>(null)

    @OptIn(InternalSerializationApi::class)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadEvent()
    }

    @OptIn(InternalSerializationApi::class)
    private fun loadEvent() {
        viewModelScope.launch {
            repository.events.collect { events ->
                _event.value = events.find { it.id == eventId }
                _isLoading.value = false
            }
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun updateEvent(
        title: String,
        description: String,
        location: String,
        dateTime: String,     // Start
        endDateTime: String,  // End <--- NOVO
        category: String,
        price: Double,
        maxCapacity: Int,
        imageBytes: ByteArray?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentEvent = _event.value ?: return
        _isLoading.value = true

        viewModelScope.launch {
            try {
                var finalImageUrl = currentEvent.imageUrl

                if (imageBytes != null) {
                    val fileName = "${Clock.System.now().toEpochMilliseconds()}_edit.jpg"
                    val url = repository.uploadEventImage(imageBytes, fileName)
                    if (url != null) finalImageUrl = url
                }

                val updatedEvent = currentEvent.copy(
                    title = title,
                    description = description,
                    location = location,
                    dateTime = dateTime,
                    endDateTime = endDateTime, // <--- GRAVA AQUI
                    category = category,
                    price = price,
                    maxCapacity = maxCapacity,
                    imageUrl = finalImageUrl
                )

                repository.addEvent(updatedEvent)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Error updating event")
            } finally {
                _isLoading.value = false
            }
        }
    }
}