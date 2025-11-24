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

class EditEventViewModel(
    private val repository: EventRepository,
    private val eventId: String
) : ViewModel() {

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadEvent()
    }

    private fun loadEvent() {
        viewModelScope.launch {
            // Procura o evento na lista (ou podia fazer um getById direto no repo)
            repository.events.collect { events ->
                _event.value = events.find { it.id == eventId }
                _isLoading.value = false
            }
        }
    }

    fun updateEvent(
        title: String,
        description: String,
        location: String,
        dateTime: String,
        category: String,
        imageBytes: ByteArray?, // Nova imagem (se houver)
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentEvent = _event.value ?: return
        _isLoading.value = true

        viewModelScope.launch {
            try {
                var finalImageUrl = currentEvent.imageUrl

                // 1. Se o utilizador escolheu uma NOVA imagem, faz upload
                if (imageBytes != null) {
                    val fileName = "${Clock.System.now().toEpochMilliseconds()}_edit.jpg"
                    val url = repository.uploadEventImage(imageBytes, fileName)
                    if (url != null) finalImageUrl = url
                }

                // 2. Cria o objeto atualizado
                val updatedEvent = currentEvent.copy(
                    title = title,
                    description = description,
                    location = location,
                    dateTime = dateTime,
                    category = category,
                    imageUrl = finalImageUrl
                )

                // 3. Grava na BD (o addEvent do repo já faz 'set' com merge se tiver ID)
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