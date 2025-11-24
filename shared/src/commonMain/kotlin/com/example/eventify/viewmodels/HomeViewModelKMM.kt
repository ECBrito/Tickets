package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.model.EventCategory // <--- Importar o Enum
import com.example.eventify.repository.EventRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModelKMM(
    private val repository: EventRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Categoria Selecionada (null = "All")
    private val _selectedCategory = MutableStateFlow<EventCategory?>(null)
    val selectedCategory: StateFlow<EventCategory?> = _selectedCategory

    private val currentUserId = Firebase.auth.currentUser?.uid ?: ""

    private val allEventsFlow = repository.events

    private val favoritesFlow = if (currentUserId.isNotEmpty()) {
        repository.getFavoriteEventIds(currentUserId)
    } else {
        flowOf(emptyList())
    }

    // Base: Eventos + Info se é favorito
    private val eventsWithFavs = combine(allEventsFlow, favoritesFlow) { events, favIds ->
        events.map { event ->
            event.copy(isSaved = favIds.contains(event.id))
        }
    }

    // 1. Featured: Sempre os primeiros 3, independentemente do filtro
    val featuredEvents: StateFlow<List<Event>> = eventsWithFavs
        .combine(favoritesFlow) { events, _ ->
            events.take(3)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Upcoming: O resto da lista, FILTRADO pela categoria
    val upcomingEvents: StateFlow<List<Event>> = combine(
        eventsWithFavs,
        _selectedCategory
    ) { events, category ->
        // Primeiro ignoramos os 3 destaques
        val remaining = events.drop(3)

        // Depois aplicamos o filtro (se houver categoria selecionada)
        if (category == null) {
            remaining
        } else {
            remaining.filter { event ->
                // Compara ignorando maiúsculas/minúsculas
                event.category.equals(category.name, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            eventsWithFavs.collect { _isLoading.value = false }
        }
    }

    // Ações
    fun selectCategory(category: EventCategory?) {
        // Se clicar na mesma categoria, desmarca (volta a All). Senão, seleciona a nova.
        if (_selectedCategory.value == category) {
            _selectedCategory.value = null
        } else {
            _selectedCategory.value = category
        }
    }

    fun toggleSave(eventId: String) {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            repository.toggleFavorite(currentUserId, eventId)
        }
    }

    fun loadData() {}
}