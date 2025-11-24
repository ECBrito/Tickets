package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi

class PurchaseViewModel(
    private val repository: EventRepository,
    private val userId: String,
    private val eventId: String
) : ViewModel() {

    @OptIn(InternalSerializationApi::class)
    private val _event = MutableStateFlow<Event?>(null)
    @OptIn(InternalSerializationApi::class)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadEvent()
    }

    @OptIn(InternalSerializationApi::class)
    private fun loadEvent() {
        viewModelScope.launch {
            repository.events.collect { events ->
                _event.value = events.find { it.id == eventId }
                _isLoading.value = false
            }
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun confirmPurchase(quantity: Int, onSuccess: () -> Unit) {
        val currentEvent = _event.value ?: return

        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.buyTickets(userId, currentEvent, quantity)
            _isLoading.value = false
            if (success) onSuccess()
        }
    }
}