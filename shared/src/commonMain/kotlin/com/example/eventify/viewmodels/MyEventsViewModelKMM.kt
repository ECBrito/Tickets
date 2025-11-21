package com.example.eventify.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepositoryKMM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MyEventsViewModelKMM(
    private val repository: EventRepositoryKMM,
    private val currentUserId: String
) : ViewModel() {

    private val _registeredEvents = MutableStateFlow<List<Event>>(emptyList())
    val registeredEvents: StateFlow<List<Event>> = _registeredEvents

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadRegisteredEvents()
    }

    fun loadRegisteredEvents() {
        _isLoading.value = true
        CoroutineScope(Dispatchers.Main).launch {
            val events = repository.getEventsRegisteredByUser(currentUserId)
            _registeredEvents.value = events
            _isLoading.value = false
        }
    }
}
