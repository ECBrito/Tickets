package com.example.eventify.ui.viewmodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import com.example.eventify.ui.screens.organizer.OrganizerStat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OrganizerViewModel : ViewModel() {

    private val repository = EventRepository

    // Estado das Estatísticas
    private val _stats = MutableStateFlow<List<OrganizerStat>>(emptyList())
    val stats: StateFlow<List<OrganizerStat>> = _stats.asStateFlow()

    // Estado dos Eventos Recentes
    private val _recentEvents = MutableStateFlow<List<Event>>(emptyList())
    val recentEvents: StateFlow<List<Event>> = _recentEvents.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            // 1. Buscar eventos onde eu sou o organizador
            val myEvents = repository.getHostedEvents()

            // 2. Calcular Estatísticas (Simuladas com base nos dados)
            val totalEvents = myEvents.size
            val upcomingCount = myEvents.count { it.dateTime.year >= 2024 } // Exemplo simples

            // Simulação de receita e registos (já que o nosso modelo Event não tem vendas reais ainda)
            val totalRevenue = myEvents.sumOf { it.price * 100 } // Mock: 100 bilhetes vendidos por evento
            val totalRegistrations = myEvents.size * 150 // Mock: 150 pessoas por evento

            _stats.value = listOf(
                OrganizerStat("Total Revenue", "$${totalRevenue.toInt()}", "+5% this month", true, Icons.Default.AttachMoney),
                OrganizerStat("Registrations", "$totalRegistrations", "+12% this month", true, Icons.Default.Group),
                OrganizerStat("Upcoming", "$upcomingCount", null, true, Icons.Default.Event),
                OrganizerStat("Hosted", "$totalEvents", null, true, Icons.Default.History)
            )

            // 3. Atualizar lista de recentes (os 3 primeiros)
            _recentEvents.value = myEvents.take(5)
        }
    }
}