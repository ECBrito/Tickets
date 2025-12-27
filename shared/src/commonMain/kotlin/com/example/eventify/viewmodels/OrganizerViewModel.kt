package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlinx.datetime.Clock

@OptIn(InternalSerializationApi::class)
class OrganizerViewModel(
    private val repository: EventRepository
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    // 1. A lista de eventos que a UI precisa
    val events: StateFlow<List<Event>> = repository.events
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Função para apagar eventos
    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
        }
    }

    fun createEvent(
        title: String,
        description: String,
        locationName: String,
        imageBytes: ByteArray?,
        dateTime: String,
        endDateTime: String,
        category: String,
        price: Double,
        maxCapacity: Int,
        latitude: Double,
        longitude: Double,
        isFeatured: Boolean,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                val imageUrl = if (imageBytes != null) {
                    repository.uploadEventImage(imageBytes, "event_${Clock.System.now().toEpochMilliseconds()}.jpg")
                } else ""

                val newEvent = Event(
                    title = title,
                    description = description,
                    locationName = locationName,
                    imageUrl = imageUrl ?: "",
                    dateTime = dateTime,
                    endDateTime = endDateTime,
                    category = category,
                    price = price,
                    maxCapacity = maxCapacity,
                    latitude = latitude,
                    longitude = longitude,
                    isFeatured = isFeatured
                )

                val success = repository.addEvent(newEvent)
                if (success) onSuccess() else onError("Erro ao guardar")
            } catch (e: Exception) {
                onError(e.message ?: "Erro")
            } finally {
                _loading.value = false
            }
        }
    }
}