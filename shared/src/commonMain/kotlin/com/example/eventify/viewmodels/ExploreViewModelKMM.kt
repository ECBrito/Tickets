package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.model.FilterState
import com.example.eventify.model.PriceType
import com.example.eventify.repository.EventRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi

class ExploreViewModelKMM(
    private val repository: EventRepository
) : ViewModel() {

    // Dados "Crus" (Eventos + Estado de Favorito já aplicado)
    @OptIn(InternalSerializationApi::class)
    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())

    // Lista final filtrada que a UI vê
    @OptIn(InternalSerializationApi::class)
    private val _filteredEvents = MutableStateFlow<List<Event>>(emptyList())
    @OptIn(InternalSerializationApi::class)
    val events: StateFlow<List<Event>> = _filteredEvents.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var _currentFilters = FilterState()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val currentUserId = Firebase.auth.currentUser?.uid ?: ""

    init {
        observeEventsWithFavorites()
    }

    @OptIn(InternalSerializationApi::class)
    private fun observeEventsWithFavorites() {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. Obtém o Flow de Favoritos (se logado)
            val favoritesFlow = if (currentUserId.isNotEmpty()) {
                repository.getFavoriteEventIds(currentUserId)
            } else {
                flowOf(emptyList())
            }

            // 2. Combina Eventos + Favoritos
            // Sempre que um evento muda OU um favorito muda, este bloco corre
            combine(repository.events, favoritesFlow) { events, favIds ->
                events.map { event ->
                    event.copy(isSaved = favIds.contains(event.id))
                }
            }.collect { mergedEvents ->
                // 3. Atualiza a lista base e reaplica os filtros de pesquisa
                _allEvents.value = mergedEvents
                applyFilters()
                _isLoading.value = false
            }
        }
    }

    // --- AÇÃO DE FAVORITO ---
    fun toggleFavorite(eventId: String) {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            repository.toggleFavorite(currentUserId, eventId)
        }
    }

    // --- FILTROS ---

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun updateFilters(newFilters: FilterState) {
        _currentFilters = newFilters
        applyFilters()
    }

    @OptIn(InternalSerializationApi::class)
    private fun applyFilters() {
        val query = _searchQuery.value.lowercase()
        val filters = _currentFilters
        val currentList = _allEvents.value

        _filteredEvents.value = currentList.filter { event ->
            // 1. Texto
            val matchesSearch = if (query.isBlank()) true else {
                event.title.lowercase().contains(query) ||
                        event.location.lowercase().contains(query) ||
                        event.category.lowercase().contains(query)
            }
            // 2. Preço
            val matchesPrice = when (filters.priceType) {
                PriceType.ANY -> true
                PriceType.FREE -> event.price == 0.0
                PriceType.PAID -> event.price > 0.0
            }
            // 3. Categoria
            val matchesCategory = if (filters.categories.isEmpty()) true else {
                filters.categories.any { it.name.equals(event.category, ignoreCase = true) }
            }

            matchesSearch && matchesPrice && matchesCategory
        }
    }
}