package com.example.eventify.viewmodels

import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepositoryKMM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModelKMM(private val repository: EventRepositoryKMM) {

    private val viewModelScope = CoroutineScope(Dispatchers.Main)

    private val _featuredEvents = MutableStateFlow<List<Event>>(emptyList())
    val featuredEvents: StateFlow<List<Event>> = _featuredEvents.asStateFlow()

    private val _upcomingEvents = MutableStateFlow<List<Event>>(emptyList())
    val upcomingEvents: StateFlow<List<Event>> = _upcomingEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            val allEvents = repository.events.value
            _featuredEvents.value = allEvents.filter { it.id.startsWith("f") }
            _upcomingEvents.value = allEvents.filter { !it.id.startsWith("f") }
            _isLoading.value = false
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.startListening()
            loadEvents()
            _isLoading.value = false
        }
    }
}