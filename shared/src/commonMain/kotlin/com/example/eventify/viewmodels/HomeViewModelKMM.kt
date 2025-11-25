package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.model.EventCategory
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
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class) // Aplica a toda a classe para limpar o código
class HomeViewModelKMM(
    private val repository: EventRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Categoria Selecionada na Home (null = "All")
    private val _selectedCategory = MutableStateFlow<EventCategory?>(null)
    val selectedCategory: StateFlow<EventCategory?> = _selectedCategory

    // Interesses do utilizador (carregados do Perfil)
    private val _userInterests = MutableStateFlow<List<String>>(emptyList())

    private val currentUserId = Firebase.auth.currentUser?.uid ?: ""

    // --- FLOWS ---

    // 1. Eventos Crus da BD
    private val allEventsFlow = repository.events

    // 2. Favoritos do User
    private val favoritesFlow = if (currentUserId.isNotEmpty()) {
        repository.getFavoriteEventIds(currentUserId)
    } else {
        flowOf(emptyList())
    }

    // 3. Base Combinada: Eventos + Estado isSaved
    private val eventsWithFavs = combine(allEventsFlow, favoritesFlow) { events, favIds ->
        events.map { event ->
            event.copy(isSaved = favIds.contains(event.id))
        }
    }

    // --- LISTAS FINAIS PARA A UI ---

    // A. Featured: Os primeiros 3 eventos (sempre visíveis)
    val featuredEvents: StateFlow<List<Event>> = eventsWithFavs
        .combine(favoritesFlow) { events, _ ->
            events.take(3)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // B. Upcoming: O resto dos eventos, FILTRADO pela categoria selecionada (Chips)
    val upcomingEvents: StateFlow<List<Event>> = combine(
        eventsWithFavs,
        _selectedCategory
    ) { events, category ->
        // Ignora os 3 primeiros que já estão no Featured
        val remaining = events.drop(3)

        if (category == null) {
            remaining
        } else {
            remaining.filter { event ->
                event.category.equals(category.name, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // C. For You: Eventos recomendados baseados nos interesses do perfil
    val forYouEvents: StateFlow<List<Event>> = combine(
        eventsWithFavs,
        _userInterests
    ) { events, interests ->
        if (interests.isEmpty()) {
            emptyList() // Se não escolheu interesses, não mostra a secção
        } else {
            events.filter { event ->
                // Verifica se a categoria do evento está na lista de interesses do user
                interests.any { userInterest ->
                    userInterest.equals(event.category, ignoreCase = true)
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // 1. Carregar Perfil para obter interesses
        viewModelScope.launch {
            if (currentUserId.isNotBlank()) {
                val profile = repository.getUserProfile(currentUserId)
                if (profile != null) {
                    _userInterests.value = profile.interests
                }
            }
        }

        // 2. Gerir Loading
        viewModelScope.launch {
            eventsWithFavs.collect { _isLoading.value = false }
        }
    }

    // --- AÇÕES ---

    fun selectCategory(category: EventCategory?) {
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