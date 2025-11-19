package com.example.eventify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class MyEventsViewModel : ViewModel() {

    private val repository = EventRepository

    // Estado reativo para eventos registados
    // Observa o Flow do repositório e filtra automaticamente quando há mudanças
    val registeredEvents: StateFlow<List<Event>> = repository.events
        .map { allEvents ->
            allEvents.filter { it.isRegistered }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Estado reativo para eventos organizados
    val hostedEvents: StateFlow<List<Event>> = repository.events
        .map { allEvents ->
            allEvents.filter { it.organizer.contains("Organizer") || it.organizer.contains("Me") || it.organizer.contains("Eu") }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // O loading já não é tão crítico porque os dados vêm do Flow instantaneamente,
    // mas mantemos para compatibilidade se quisermos adicionar chamadas de rede depois.
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading // No momento, sempre false pois é local
}