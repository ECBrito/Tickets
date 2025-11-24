package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.model.Ticket
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.InternalSerializationApi
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

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

    @OptIn(InternalSerializationApi::class)
    private val _event = MutableStateFlow<Event?>(null)
    @OptIn(InternalSerializationApi::class)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _stats = MutableStateFlow(EventStats())
    val stats: StateFlow<EventStats> = _stats.asStateFlow()

    private val _selectedTimeRange = MutableStateFlow(TimeRange.DAYS_7)
    val selectedTimeRange: StateFlow<TimeRange> = _selectedTimeRange.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Cache local dos bilhetes
    @OptIn(InternalSerializationApi::class)
    private var allTickets: List<Ticket> = emptyList()

    init {
        loadDashboardData()
    }

    @OptIn(InternalSerializationApi::class)
    private fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. Carregar Evento
            repository.events.collect { events ->
                val foundEvent = events.find { it.id == eventId }
                _event.value = foundEvent

                if (foundEvent != null) {
                    // 2. Carregar TODOS os bilhetes deste evento (uma vez)
                    allTickets = repository.getTicketsForEvent(eventId)

                    // 3. Calcular stats iniciais
                    calculateStats(_selectedTimeRange.value, foundEvent)
                }
                _isLoading.value = false
            }
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun setTimeRange(range: TimeRange) {
        _selectedTimeRange.value = range
        _event.value?.let { calculateStats(range, it) }
    }

    @OptIn(InternalSerializationApi::class)
    private fun calculateStats(range: TimeRange, event: Event) {
        val now = Clock.System.now()

        // Definir janelas de tempo (Atual vs Anterior para calcular crescimento)
        val (startTimeCurrent, startTimePrevious) = when (range) {
            TimeRange.HOURS_24 -> Pair(now.minus(24.hours), now.minus(48.hours))
            TimeRange.DAYS_7 -> Pair(now.minus(7.days), now.minus(14.days))
            TimeRange.DAYS_30 -> Pair(now.minus(30.days), now.minus(60.days))
            TimeRange.ALL -> Pair(Instant.fromEpochMilliseconds(0), Instant.fromEpochMilliseconds(0))
        }

        // Filtros de Data
        val ticketsCurrentPeriod = filterTicketsByDate(allTickets, startTimeCurrent.toEpochMilliseconds(), now.toEpochMilliseconds())
        val ticketsPreviousPeriod = if (range == TimeRange.ALL) emptyList() else filterTicketsByDate(allTickets, startTimePrevious.toEpochMilliseconds(), startTimeCurrent.toEpochMilliseconds())

        // --- CÁLCULOS ---

        // 1. Attendees (Total de bilhetes vendidos no período)
        val currentCount = ticketsCurrentPeriod.size
        val previousCount = ticketsPreviousPeriod.size
        val attendeesGrowth = calculateGrowth(currentCount.toDouble(), previousCount.toDouble())

        // 2. Sales (Dinheiro ganho: Bilhetes * Preço)
        val currentSales = currentCount * event.price
        val previousSales = previousCount * event.price
        val salesGrowth = calculateGrowth(currentSales, previousSales)

        // 3. Capacity (Sempre baseada no total ABSOLUTO)
        val totalSold = allTickets.size

        // 4. Shares (Se adicionaste o campo 'shares' ao Evento no passo anterior)
        // Se não adicionaste, usa 0 ou um valor mock
        val currentShares = 0 // event.shares

        _stats.value = EventStats(
            totalAttendees = currentCount,
            attendeesGrowth = attendeesGrowth,
            ticketSales = currentSales,
            salesGrowth = salesGrowth,
            capacityCurrent = totalSold,
            // Se ainda não atualizaste o Event.kt com maxCapacity, usa 100 como default
            capacityMax = 100, // event.maxCapacity

            // Placeholders (exigiriam tabela de Analytics separada)
            engagementScore = 8.5,
            engagementGrowth = 1.2,
            socialShares = currentShares,
            sharesGrowth = 0.0
        )
    }

    @OptIn(InternalSerializationApi::class)
    private fun filterTicketsByDate(tickets: List<Ticket>, start: Long, end: Long): List<Ticket> {
        return tickets.filter { it.purchaseDate in start..end }
    }

    private fun calculateGrowth(current: Double, previous: Double): Double {
        if (previous == 0.0) return if (current > 0) 100.0 else 0.0
        return ((current - previous) / previous) * 100
    }
}