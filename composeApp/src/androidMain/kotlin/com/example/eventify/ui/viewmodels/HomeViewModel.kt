package com.example.eventify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val repository = EventRepository

    private val _featuredEvents = MutableStateFlow<List<Event>>(emptyList())
    val featuredEvents: StateFlow<List<Event>> = _featuredEvents.asStateFlow()

    private val _upcomingEvents = MutableStateFlow<List<Event>>(emptyList())
    val upcomingEvents: StateFlow<List<Event>> = _upcomingEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            // REMOVIDO: O "if isEmpty" que impedia a atualização
            // Agora recarrega sempre que a função é chamada

            // Loading rápido para feedback visual (opcional)
            // _isLoading.value = true
            // delay(500)

            _featuredEvents.value = repository.getFeaturedEvents()
            _upcomingEvents.value = repository.getUpcomingEvents() // Isto agora traz os eventos novos

            _isLoading.value = false
        }
    }

    fun toggleSave(eventId: String) {
        // TODO: Implementar
    }
}