package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.model.FilterState
import com.example.eventify.model.PriceType
// Nota: O EventCategory não precisa de import explícito se vier dentro do FilterState,
// mas se precisares, garante que só importas uma vez.
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExploreViewModelKMM(
    private val repository: EventRepository
) : ViewModel() {

    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())

    // Lista final que a UI vê
    private val _filteredEvents = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _filteredEvents.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Estado dos Filtros Avançados
    private var _currentFilters = FilterState()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeEvents()
    }

    private fun observeEvents() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.events.collect { fetchedEvents ->
                _allEvents.value = fetchedEvents
                applyFilters() // Reaplica filtros quando chegam dados novos
                _isLoading.value = false
            }
        }
    }

    // --- Ações da UI ---

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        applyFilters()
    }

    fun updateFilters(newFilters: FilterState) {
        _currentFilters = newFilters
        applyFilters()
    }

    // --- Lógica Central de Filtragem ---

    private fun applyFilters() {
        val query = _searchQuery.value.lowercase()
        val filters = _currentFilters

        val currentList = _allEvents.value

        _filteredEvents.value = currentList.filter { event ->
            // 1. Filtro de Texto (Search Bar)
            val matchesSearch = if (query.isBlank()) true else {
                event.title.lowercase().contains(query) ||
                        event.location.lowercase().contains(query) ||
                        event.category.lowercase().contains(query)
            }

            // 2. Filtro de Preço
            val matchesPrice = when (filters.priceType) {
                PriceType.ANY -> true
                PriceType.FREE -> event.price == 0.0
                PriceType.PAID -> event.price > 0.0
            }

            // 3. Filtro de Categoria
            val matchesCategory = if (filters.categories.isEmpty()) {
                true
            } else {
                // Compara o nome da categoria (ex: "MUSIC" vs "Music")
                filters.categories.any { it.name.equals(event.category, ignoreCase = true) }
            }

            matchesSearch && matchesPrice && matchesCategory
        }
    }
}