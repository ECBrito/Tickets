package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.eventify.model.Category
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
class OrganizerViewModel(
    private val repository: EventRepository // <--- Nome da interface corrigido
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Mantemos duas listas: Todos os eventos (raw) e os Filtrados (display)
    @OptIn(InternalSerializationApi::class)
    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    @OptIn(InternalSerializationApi::class)
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    init {
        // Coleção reativa dos eventos
        viewModelScope.launch {
            _isLoading.value = true

            // O 'collect' substitui o 'startListening' antigo
            repository.events.collect { list ->
                _allEvents.value = list
                _events.value = list // Inicialmente mostra tudo
                _isLoading.value = false
            }
        }
    }

    fun createEvent(
        id: String, // Nota: Se o ID for vazio "", o repo gera um novo
        title: String,
        description: String,
        location: String,
        imageUrl: String,
        dateTime: String,
        category: Category
    ) {
        val newEvent = Event(
            id = id,
            title = title,
            description = description,
            location = location,
            imageUrl = imageUrl,
            dateTime = dateTime,
            category = category.name, // Assume que Category é um Enum ou tem .name
            registeredUserIds = emptyList(),
            isRegistered = false
        )

        viewModelScope.launch {
            _isLoading.value = true
            repository.addEvent(newEvent)
            _isLoading.value = false
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // O repositório já tem esta função implementada desde o início!
            repository.deleteEvent(eventId)
            // Como o Flow 'events' é em tempo real, a lista atualiza-se sozinha
            _isLoading.value = false
        }
    }

    fun toggleEventRegistration(eventId: String, userId: String) {
        viewModelScope.launch {
            repository.toggleEventRegistration(eventId, userId)
        }
    }

    fun searchEvents(query: String) {
        // Filtramos a lista localmente em vez de chamar o repositório
        // Isto é mais rápido e reativo
        val currentList = _allEvents.value

        if (query.isBlank()) {
            _events.value = currentList
        } else {
            _events.value = repository.searchEvents(query, currentList)
        }
    }

}