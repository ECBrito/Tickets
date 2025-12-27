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

class CreateEventViewModel(
    private val repository: EventRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    @OptIn(InternalSerializationApi::class)
    fun createEvent(
        title: String,
        description: String,
        locationName: String, // Nome atualizado
        imageBytes: ByteArray?,
        dateTime: String,
        endDateTime: String,
        category: String,
        price: Double,
        maxCapacity: Int,
        latitude: Double,    // Novo
        longitude: Double,   // Novo
        isFeatured: Boolean, // Novo
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _loading.value = true
            try {
                // 1. Upload da imagem
                val imageUrl = if (imageBytes != null) {
                    repository.uploadEventImage(imageBytes, "event_${Clock.System.now().toEpochMilliseconds()}.jpg")
                } else ""

                // 2. Criar objeto Event (Garante que os nomes batem com o Event.kt)
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
                    isFeatured = isFeatured,
                    organizerId = currentUserId
                )

                val success = repository.addEvent(newEvent)
                if (success) onSuccess() else onError("Erro ao guardar na base de dados")

            } catch (e: Exception) {
                onError(e.message ?: "Erro desconhecido")
            } finally {
                _loading.value = false
            }
        }
    }
}