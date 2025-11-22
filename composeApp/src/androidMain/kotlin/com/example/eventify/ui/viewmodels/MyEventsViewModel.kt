package com.example.eventify.ui.viewmodels // Confirma se o package está correto

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository // <--- Interface correta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyEventsViewModelKMM(
    private val repository: EventRepository, // <--- Nome corrigido
    private val userId: String
) : ViewModel() { // <--- Herança de ViewModel

    private val _myEvents = MutableStateFlow<List<Event>>(emptyList())
    val myEvents: StateFlow<List<Event>> = _myEvents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadMyEvents()
    }

    private fun loadMyEvents() {
        viewModelScope.launch {
            _isLoading.value = true

            // Observa o Flow do repositório
            repository.events.collect { allEvents ->
                // Filtra os eventos onde o ID do utilizador está na lista de inscritos
                val filteredList = allEvents.filter { event ->
                    event.registeredUserIds.contains(userId)
                }

                // Mapeia para garantir que a flag isRegistered está true para a UI
                _myEvents.value = filteredList.map { it.copy(isRegistered = true) }

                _isLoading.value = false
            }
        }
    }
}