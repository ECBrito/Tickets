package com.example.eventify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Category
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepositoryKMM
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class OrganizerViewModel(
    private val repository: EventRepositoryKMM
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    init {
        // Coleção reativa dos eventos
        viewModelScope.launch {
            repository.events.collect { list ->
                _events.value = list
            }
        }

        // Começar a escutar Firestore
        viewModelScope.launch {
            _isLoading.value = true
            repository.startListening()
            _isLoading.value = false
        }
    }

    fun createEvent(
        id: String,
        title: String,
        description: String,
        location: String,
        imageUrl: String,
        dateTime: String,  // ISO 8601
        category: Category
    ) {
        val newEvent = Event(
            id = id,
            title = title,
            description = description,
            location = location,
            imageUrl = imageUrl,
            dateTime = dateTime,
            category = category,
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
            repository.deleteEvent(eventId)
            _isLoading.value = false
        }
    }

    fun toggleEventRegistration(eventId: String, userId: String) {
        viewModelScope.launch {
            repository.toggleEventRegistration(eventId, userId)
        }
    }

    fun searchEvents(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val filtered = repository.searchEvents(query)
            _events.value = filtered
            _isLoading.value = false
        }
    }
}
