package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope // Importante: usa o scope correto
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// 1. Herdar de ViewModel() para aproveitar o ciclo de vida
class HomeViewModelKMM(
    private val repository: EventRepository
) : ViewModel() {

    private val _featuredEvents = MutableStateFlow<List<Event>>(emptyList())
    val featuredEvents: StateFlow<List<Event>> = _featuredEvents.asStateFlow()

    private val _upcomingEvents = MutableStateFlow<List<Event>>(emptyList())
    val upcomingEvents: StateFlow<List<Event>> = _upcomingEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(true) // Começa true para carregar logo
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        // Começa a ouvir os eventos assim que o ViewModel é criado
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            _isLoading.value = true

            // 2. "Collect" no Flow do repositório.
            // Isto fica vivo e atualiza a UI sempre que o Firebase mudar.
            repository.events.collect { allEvents ->

                // Lógica de filtro (podes ajustar esta lógica se quiseres critérios reais)
                _featuredEvents.value = allEvents.take(3) // Ex: Primeiros 3 são destaque
                _upcomingEvents.value = allEvents.drop(3) // O resto são "próximos"

                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        // Como o Flow (.collect acima) é em tempo real,
        // "refresh" geralmente não é necessário no Firebase.
        // Mas se quiseres forçar um loading visual:
        viewModelScope.launch {
            _isLoading.value = true
            // Simula um pequeno delay ou re-executa lógica se necessário
            kotlinx.coroutines.delay(500)
            _isLoading.value = false
        }
    }
}