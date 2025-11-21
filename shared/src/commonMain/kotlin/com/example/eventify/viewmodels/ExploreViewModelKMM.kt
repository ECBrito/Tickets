package com.example.eventify.viewmodels

import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepositoryKMM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ExploreViewModelKMM(
    private val repository: EventRepositoryKMM
) {

    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        viewModelScope.launch {
            repository.events.collect { list ->
                _events.value = list
                _isLoading.value = false
            }
        }
    }

    // Filtro simples
    fun filterEvents(query: String) {
        viewModelScope.launch {
            val allEvents = repository.events.value
            val filtered = if (query.isBlank()) allEvents
            else allEvents.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true) ||
                        it.location.contains(query, ignoreCase = true)
            }
            _events.value = filtered
        }
    }
}