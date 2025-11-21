package com.example.eventify.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepositoryKMM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModelKMM(private val repository: EventRepositoryKMM) {

    // CoroutineScope do KMM
    private val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered events
    private val _filteredEvents = MutableStateFlow<List<Event>>(emptyList())
    val filteredEvents: StateFlow<List<Event>> = _filteredEvents.asStateFlow()

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAllEvents()
    }

    private fun loadAllEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.startListening()
            repository.events.collect { events ->
                updateFilteredEvents(events, _searchQuery.value)
            }
            _isLoading.value = false
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        updateFilteredEvents(repository.events.value, query)
    }

    private fun updateFilteredEvents(allEvents: List<Event>, query: String) {
        val filtered = if (query.isBlank()) allEvents
        else allEvents.filter { e ->
            e.title.contains(query, ignoreCase = true) ||
                    e.description.contains(query, ignoreCase = true) ||
                    e.location.contains(query, ignoreCase = true)
        }
        _filteredEvents.value = filtered
    }
}
