package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine // <--- Importante
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModelKMM(
    private val repository: EventRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // User ID atual (para saber quais favoritos buscar)
    private val currentUserId = Firebase.auth.currentUser?.uid ?: ""

    // 1. Flow dos Eventos (Cru)
    private val allEventsFlow = repository.events

    // 2. Flow dos Favoritos (IDs)
    private val favoritesFlow = if (currentUserId.isNotEmpty()) {
        repository.getFavoriteEventIds(currentUserId)
    } else {
        // Se não estiver logado, flow vazio
        kotlinx.coroutines.flow.flowOf(emptyList())
    }

    // 3. COMBINAÇÃO: Eventos + Favoritos = Eventos Prontos para UI
    // O 'combine' junta os dois flows. Sempre que um muda, ele recalcula.
    private val eventsWithFavs = combine(allEventsFlow, favoritesFlow) { events, favIds ->
        events.map { event ->
            // Verifica se o ID deste evento está na lista de favoritos
            event.copy(isSaved = favIds.contains(event.id))
        }
    }

    // Agora expomos as listas já filtradas e marcadas
    val featuredEvents: StateFlow<List<Event>> = eventsWithFavs
        .combine(favoritesFlow) { events, _ ->
            // Logica de destaque (ex: primeiros 3 ou campo isFeatured)
            events.take(3)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingEvents: StateFlow<List<Event>> = eventsWithFavs
        .combine(favoritesFlow) { events, _ ->
            events.drop(3)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // O stateIn trata do loading inicial, mas podemos forçar update ao isLoading
        viewModelScope.launch {
            eventsWithFavs.collect {
                _isLoading.value = false
            }
        }
    }

    // Ação da UI
    fun toggleSave(eventId: String) {
        if (currentUserId.isBlank()) return // Ou mandar para login

        viewModelScope.launch {
            repository.toggleFavorite(currentUserId, eventId)
        }
    }

    // Função dummy para compatibilidade se a UI ainda chamar loadData
    fun loadData() {}
}