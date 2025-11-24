package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Attendee
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AttendeesViewModel(
    private val repository: EventRepository,
    private val eventId: String
) : ViewModel() {

    private val _allAttendees = MutableStateFlow<List<Attendee>>(emptyList())

    private val _filteredAttendees = MutableStateFlow<List<Attendee>>(emptyList())
    val attendees: StateFlow<List<Attendee>> = _filteredAttendees.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadAttendees()
    }

    fun loadAttendees() {
        viewModelScope.launch {
            _isLoading.value = true
            val list = repository.getEventAttendees(eventId)
            _allAttendees.value = list
            filterList(_searchQuery.value)
            _isLoading.value = false
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        filterList(query)
    }

    private fun filterList(query: String) {
        if (query.isBlank()) {
            _filteredAttendees.value = _allAttendees.value
        } else {
            _filteredAttendees.value = _allAttendees.value.filter {
                it.name.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
            }
        }
    }

    fun manualCheckIn(ticketId: String) {
        viewModelScope.launch {
            val success = repository.manualCheckIn(ticketId)
            if (success) {
                // Atualiza a lista localmente para refletir a mudança instantaneamente
                val updatedList = _allAttendees.value.map {
                    if (it.ticketId == ticketId) it.copy(isCheckedIn = true) else it
                }
                _allAttendees.value = updatedList
                filterList(_searchQuery.value)
            }
        }
    }
}