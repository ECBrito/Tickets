package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Import crucial
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository // Interface correta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CreateEventViewModel(
    private val repository: EventRepository,
    private val organizerId: String
) : ViewModel() { // 1. Herança correta

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    fun createEvent(
        title: String,
        description: String,
        location: String,
        imageUrl: String?,
        dateTime: String,
        category: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        // Validação básica
        if (title.isBlank() || location.isBlank()) {
            onError?.invoke("Título e Localização são obrigatórios")
            return
        }

        val event = Event(
            id = "", // O Firebase gera isto automaticamente
            title = title,
            description = description,
            location = location,
            imageUrl = imageUrl ?: "",
            dateTime = dateTime,
            category = category,
            // Adiciona logo o criador à lista de inscritos
            registeredUserIds = listOf(organizerId),
            isRegistered = true
        )

        _loading.value = true

        viewModelScope.launch {
            try {
                // 2. Verifica o resultado booleano do repositório
                val success = repository.addEvent(event)

                if (success) {
                    onSuccess?.invoke()
                } else {
                    onError?.invoke("Falha ao criar evento. Tente novamente.")
                }
            } catch (e: Exception) {
                onError?.invoke(e.message ?: "Erro desconhecido")
            } finally {
                _loading.value = false
            }
        }
    }
}