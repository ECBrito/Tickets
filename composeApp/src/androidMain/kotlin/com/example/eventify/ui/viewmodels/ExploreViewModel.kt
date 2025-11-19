package com.example.eventify.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.model.FilterState
import com.example.eventify.model.PriceType
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ExploreViewModel : ViewModel() {

    // MUDANÇA: Referência ao Singleton
    private val repository = EventRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())

    // O 'combine' vai reagir sempre que o repositório for atualizado?
    // Numa arquitetura reativa real (Room/Flow), sim. Aqui com Listas estáticas,
    // precisamos de um "trigger" para recarregar se adicionarmos eventos.
    // Para simplificar, assumimos que a busca inicial já traz os dados novos.

    val filteredEvents: StateFlow<List<Event>> = combine(
        _searchQuery,
        _filterState
    ) { query, filters ->
        // Busca sempre a lista mais atual do repositório
        var events = repository.searchEvents(query)

        if (filters.categories.isNotEmpty()) {
            events = events.filter { event -> filters.categories.contains(event.category) }
        }

        if (filters.priceType != PriceType.ALL) {
            events = events.filter { event ->
                when (filters.priceType) {
                    PriceType.FREE -> event.price == 0.0
                    PriceType.PAID -> event.price > 0.0
                    else -> true
                }
            }
        }

        val dateFrom = filters.dateFrom
        if (dateFrom != null) {
            events = events.filter { event -> event.dateTime.date >= dateFrom }
        }

        val dateTo = filters.dateTo
        if (dateTo != null) {
            events = events.filter { event -> event.dateTime.date <= dateTo }
        }

        events
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onFilterApply(newFilters: FilterState) {
        _filterState.value = newFilters
    }

    // Função para forçar atualização (útil quando voltamos de criar evento)
    fun refresh() {
        // Uma forma simples de re-disparar o fluxo é redefinir a query para ela mesma
        _searchQuery.value = _searchQuery.value
    }
}