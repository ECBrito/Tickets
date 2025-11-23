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

// Modelo de dados simples para as estatísticas
data class EventStats(
    val totalAttendees: Int = 0,
    val attendeesGrowth: Double = 0.0,
    val ticketSales: Double = 0.0,
    val salesGrowth: Double = 0.0,
    val capacityCurrent: Int = 0,
    val capacityMax: Int = 0,
    val engagementScore: Double = 0.0,
    val engagementGrowth: Double = 0.0,
    val socialShares: Int = 0,
    val sharesGrowth: Double = 0.0
)

enum class TimeRange { HOURS_24, DAYS_7, DAYS_30, ALL }

class OrganizerEventDashboardViewModel(
    private val repository: EventRepository,
    private val eventId: String
) : ViewModel() {

    private val _event = MutableStateFlow<Event?>(null)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _stats = MutableStateFlow(EventStats())
    val stats: StateFlow<EventStats> = _stats.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimeRange.DAYS_7)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. Carregar dados básicos do evento
            // (Num cenário real, o repositorio buscaria o evento pelo ID)
            repository.events.collect { events ->
                val foundEvent = events.find { it.id == eventId }
                _event.value = foundEvent

                // 2. Simular cálculo de estatísticas baseado no evento e no TimeRange
                // Isto simula o delay da rede
                if (foundEvent != null) {
                    updateStatsForRange(_selectedTimeRange.value)
                }
                _isLoading.value = false
            }
        }
    }

    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
        viewModelScope.launch {
            _isLoading.value = true
            delay(300) // Fake loading para UX
            updateStatsForRange(range)
            _isLoading.value = false
        }
    }

    private fun updateStatsForRange(range: TimeRange) {
        // Lógica MOCK para variar os números quando mudas as abas
        val multiplier = when(range) {
            TimeRange.HOURS_24 -> 0.1
            TimeRange.DAYS_7 -> 1.0
            TimeRange.DAYS_30 -> 3.5
            TimeRange.ALL -> 5.0
        }

        _stats.value = EventStats(
            totalAttendees = (1250 * multiplier).toInt(),
            attendeesGrowth = 5.2,
            ticketSales = 85400.0 * multiplier,
            salesGrowth = 12.8,
            capacityCurrent = (1250 * multiplier).toInt().coerceAtMost(1500),
            capacityMax = 1500,
            engagementScore = 8.2,
            engagementGrowth = -1.5,
            socialShares = (789 * multiplier).toInt(),
            sharesGrowth = 21.0
        )
    }
}