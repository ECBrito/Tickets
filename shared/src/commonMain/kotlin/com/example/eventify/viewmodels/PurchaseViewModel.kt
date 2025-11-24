package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.delay
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

    // Estado de processamento do pagamento
    private val _isProcessingPayment = MutableStateFlow(false)
    val isProcessingPayment: StateFlow<Boolean> = _isProcessingPayment.asStateFlow()

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
    fun processPaymentAndPurchase(quantity: Int, onSuccess: () -> Unit) {
        val currentEvent = _event.value ?: return

        viewModelScope.launch {
            // 1. Simular contacto com o Banco (Loading visual)
            _isProcessingPayment.value = true
            delay(2000) // Espera 2 segundos (Simulação)

            // 2. Se o pagamento "passou", cria os bilhetes na BD
            val success = repository.buyTickets(userId, currentEvent, quantity)

            _isProcessingPayment.value = false

            if (success) {
                onSuccess()
            }
        }
    }
}