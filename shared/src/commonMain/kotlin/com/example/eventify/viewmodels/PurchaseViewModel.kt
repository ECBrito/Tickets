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

    // --- Lógica de Descontos ---
    private val _discountPercent = MutableStateFlow(0) // 0 a 100
    val discountPercent: StateFlow<Int> = _discountPercent.asStateFlow()

    private val _promoMessage = MutableStateFlow("") // "Código Aplicado!" ou "Inválido"
    val promoMessage: StateFlow<String> = _promoMessage.asStateFlow()
    // ---------------------------

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

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

    // Função para aplicar código
    @OptIn(InternalSerializationApi::class)
    fun applyPromoCode(code: String) {
        if (code.isBlank()) return

        viewModelScope.launch {
            _promoMessage.value = "Checking..."
            val promo = repository.verifyPromoCode(code.trim())

            if (promo != null) {
                _discountPercent.value = promo.discountPercent
                _promoMessage.value = "Success! ${promo.discountPercent}% OFF"
            } else {
                _discountPercent.value = 0
                _promoMessage.value = "Invalid or expired code."
            }
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun processPaymentAndPurchase(quantity: Int, onSuccess: () -> Unit) {
        val currentEvent = _event.value ?: return

        viewModelScope.launch {
            _isProcessingPayment.value = true
            delay(2000) // Simulação de pagamento

            // Nota: Aqui podias guardar na BD que o bilhete foi comprado com desconto
            val success = repository.buyTickets(userId, currentEvent, quantity)

            _isProcessingPayment.value = false
            if (success) onSuccess()
        }
    }
}