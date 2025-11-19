package com.example.eventify.repository

import com.example.eventify.model.Event
import com.example.eventify.model.EventCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDateTime

// Singleton para manter os dados em memória
object EventRepository {

    // Lista inicial de dados mockados
    private val initialEvents = listOf(
        Event("f1", "Indie Fest 2024", EventCategory.FESTIVAL, "Green Park", LocalDateTime(2024, 8, 10, 18, 0), "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500", 95.0, "USD", false, false, "LiveNation"),
        Event("f2", "Future of Tech", EventCategory.CONFERENCE, "Convention Center", LocalDateTime(2024, 8, 12, 9, 0), "https://images.unsplash.com/photo-1528605248644-14dd04022da1?w=500", 250.0, "USD", true, false, "TechConf"),
        Event("f3", "Symphony Stars", EventCategory.CONCERT, "Grand Amphitheater", LocalDateTime(2024, 8, 15, 20, 0), "https://images.unsplash.com/photo-1557765957-4275f101418e?w=500", 120.0, "USD", false, false, "City Orchestra"),
        Event("u1", "Live Jazz Night", EventCategory.CONCERT, "The Blue Note Club", LocalDateTime(2024, 8, 9, 20, 0), "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500", 50.0, "USD", true, true, "Jazz Fest Inc."),
        Event("u2", "Modern Art Exhibit", EventCategory.WORKSHOP, "Metropolitan Art", LocalDateTime(2024, 8, 10, 10, 0), "https://images.unsplash.com/photo-1549492423-400259a5e319?w=500", 25.0, "USD", false, false, "Art Society"),
        Event("e1", "Stellar Sound Festival", EventCategory.FESTIVAL, "Quantum Arena", LocalDateTime(2024, 12, 14, 20, 0), "https://images.unsplash.com/photo-1524368535928-5b5e00ddc76b?w=500", 75.0, "USD", false, false, "LiveNation"),
        Event("e2", "Code & Coffee", EventCategory.CONFERENCE, "The Digital Hub", LocalDateTime(2024, 12, 15, 10, 0), "https://images.unsplash.com/photo-1542744173-8e7e53415bb0?w=500", 0.0, "USD", true, false, "Dev Community"),
        Event("e3", "Artisan Gallery", EventCategory.WORKSHOP, "Canvas Collective", LocalDateTime(2024, 12, 20, 18, 0), "https://images.unsplash.com/photo-1549492423-400259a5e319?w=500", 25.0, "USD", false, false, "Art Society"),
        Event("h1", "My Cool Workshop", EventCategory.WORKSHOP, "Home Studio", LocalDateTime(2024, 9, 20, 14, 0), "https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=500", 0.0, "USD", false, false, "Me (Organizer)")
    )

    // O StateFlow guarda uma List imutável. Para alterar, criamos uma nova lista.
    private val _eventsFlow = MutableStateFlow(initialEvents)
    val events = _eventsFlow.asStateFlow()

    // --- Métodos de Acesso Síncrono (Lêem o valor atual do Flow) ---

    fun getAllEvents(): List<Event> = _eventsFlow.value

    fun getFeaturedEvents(): List<Event> = _eventsFlow.value.filter { it.id.startsWith("f") }
    fun getUpcomingEvents(): List<Event> = _eventsFlow.value.filter { !it.id.startsWith("f") }

    fun getEventById(id: String): Event? = _eventsFlow.value.find { it.id == id }

    fun searchEvents(query: String): List<Event> {
        if (query.isBlank()) return _eventsFlow.value
        return _eventsFlow.value.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.location.contains(query, ignoreCase = true)
        }
    }

    fun getRegisteredEvents(): List<Event> = _eventsFlow.value.filter { it.isRegistered }
    fun getHostedEvents(): List<Event> = _eventsFlow.value.filter { it.organizer.contains("Organizer") || it.organizer.contains("Me") }

    // --- Métodos de Mutação (Atualizam o Flow) ---

    fun addEvent(event: Event) {
        // Cria uma nova lista mutável baseada na atual, adiciona o item e atualiza o Flow
        val currentList = _eventsFlow.value.toMutableList()
        currentList.add(0, event)
        _eventsFlow.value = currentList
    }

    fun toggleEventRegistration(eventId: String) {
        val currentList = _eventsFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == eventId }

        if (index != -1) {
            val event = currentList[index]
            // Substitui o evento na posição index por uma cópia com o estado invertido
            currentList[index] = event.copy(isRegistered = !event.isRegistered)
            _eventsFlow.value = currentList // Emite a nova lista para a UI
        }
    }
}