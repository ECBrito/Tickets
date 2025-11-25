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
    private val organizerId: String
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    @OptIn(InternalSerializationApi::class)
    fun createEvent(
        title: String,
        description: String,
        location: String,
        imageUrl: String?,
        imageBytes: ByteArray?,
        dateTime: String,    // Data de Início
        endDateTime: String, // <--- NOVO PARÂMETRO (Data de Fim)
        category: String,
        price: Double = 0.0,
        maxCapacity: Int = 100,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (title.isBlank() || location.isBlank()) {
            onError?.invoke("Título e Localização são obrigatórios")
            return
        }

        _loading.value = true

        viewModelScope.launch {
            try {
                var finalImageUrl = ""

                // 1. UPLOAD
                if (imageBytes != null) {
                    val fileName = "${Clock.System.now().toEpochMilliseconds()}.jpg"
                    val uploadedUrl = repository.uploadEventImage(imageBytes, fileName)
                    if (uploadedUrl != null) finalImageUrl = uploadedUrl
                }

                // 2. CRIAR EVENTO
                val event = Event(
                    id = "",
                    title = title,
                    description = description,
                    location = location,
                    imageUrl = finalImageUrl,
                    dateTime = dateTime,
                    endDateTime = endDateTime, // <--- GRAVAR NA BD
                    category = category,
                    organizerId = organizerId,
                    registeredUserIds = listOf(organizerId),
                    isRegistered = true,
                    price = price,
                    maxCapacity = maxCapacity,
                    shares = 0
                )

                // 3. GRAVAR
                val success = repository.addEvent(event)

                if (success) {
                    onSuccess?.invoke()
                } else {
                    onError?.invoke("Falha ao gravar evento na base de dados.")
                }

            } catch (e: Exception) {
                onError?.invoke(e.message ?: "Erro desconhecido durante a criação.")
            } finally {
                _loading.value = false
            }
        }
    }
}