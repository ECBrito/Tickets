package com.example.eventify.repository

import com.example.eventify.db.AppDatabase
import com.example.eventify.db.DatabaseDriverFactory
import com.example.eventify.db.EventEntity
import com.example.eventify.model.Event
import com.example.eventify.model.EventCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.LocalDateTime
// Removemos imports incorretos de parse e isoDayOrTimeStr

object EventRepository {

    private var db: AppDatabase? = null

    private val _eventsFlow = MutableStateFlow<List<Event>>(emptyList())
    val events = _eventsFlow.asStateFlow()

    // --- INICIALIZAÇÃO ---

    fun initialize(driverFactory: DatabaseDriverFactory) {
        if (db != null) return

        val driver = driverFactory.createDriver()
        val database = AppDatabase(driver)
        db = database

        if (database.eventDatabaseQueries.getAllEvents().executeAsList().isEmpty()) {
            insertMockData(database)
        }

        refreshCache()
    }

    private fun refreshCache() {
        val entities = db?.eventDatabaseQueries?.getAllEvents()?.executeAsList() ?: emptyList()
        _eventsFlow.value = entities.map { mapEntityToEvent(it) }
    }

    // --- CRUD ---

    fun addEvent(event: Event) {
        db?.eventDatabaseQueries?.insertEvent(
            id = event.id,
            title = event.title,
            category = event.category.name,
            location = event.location,
            dateISO = event.dateTime.toString(),
            imageUrl = event.imageUrl,
            price = event.price,
            currency = event.currency,
            isSaved = event.isSaved,
            isRegistered = event.isRegistered,
            organizer = event.organizer
        )
        refreshCache()
    }

    fun toggleEventRegistration(eventId: String) {
        val event = getEventById(eventId) ?: return
        val newStatus = !event.isRegistered

        db?.eventDatabaseQueries?.updateRegistration(
            isRegistered = newStatus,
            id = eventId
        )
        refreshCache()
    }

    // --- GETTERS ---

    fun getAllEvents(): List<Event> = _eventsFlow.value
    fun getFeaturedEvents(): List<Event> = _eventsFlow.value.filter { it.id.startsWith("f") }
    fun getUpcomingEvents(): List<Event> = _eventsFlow.value.filter { !it.id.startsWith("f") }
    fun getEventById(id: String): Event? = _eventsFlow.value.find { it.id == id }
    fun getRegisteredEvents(): List<Event> = _eventsFlow.value.filter { it.isRegistered }
    fun getHostedEvents(): List<Event> = _eventsFlow.value.filter { it.organizer.contains("Organizer") || it.organizer.contains("Me") }

    fun searchEvents(query: String): List<Event> {
        if (query.isBlank()) return _eventsFlow.value
        return _eventsFlow.value.filter {
            it.title.contains(query, ignoreCase = true) ||
                    it.location.contains(query, ignoreCase = true)
        }
    }

    // --- MAPPERS ---

    private fun mapEntityToEvent(entity: EventEntity): Event {
        return Event(
            id = entity.id,
            title = entity.title,
            category = try { EventCategory.valueOf(entity.category) } catch (e: Exception) { EventCategory.OTHER },
            location = entity.location,
            // CORREÇÃO: Usar LocalDateTime.parse(String) corretamente
            dateTime = try { LocalDateTime.parse(entity.dateISO) } catch (e: Exception) { LocalDateTime(2024, 1, 1, 12, 0) },
            imageUrl = entity.imageUrl,
            price = entity.price,
            currency = entity.currency,
            isSaved = entity.isSaved,
            isRegistered = entity.isRegistered,
            organizer = entity.organizer
        )
    }

    private fun insertMockData(database: AppDatabase) {
        val mocks = listOf(
            Event("f1", "Indie Fest 2024", EventCategory.FESTIVAL, "Green Park", LocalDateTime(2024, 8, 10, 18, 0), "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=500", 95.0, "USD", false, false, "LiveNation"),
            Event("u1", "Live Jazz Night", EventCategory.CONCERT, "The Blue Note Club", LocalDateTime(2024, 8, 9, 20, 0), "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500", 50.0, "USD", false, false, "Jazz Fest Inc."),
            Event("u2", "Code & Coffee", EventCategory.CONFERENCE, "The Digital Hub", LocalDateTime(2024, 12, 15, 10, 0), "https://images.unsplash.com/photo-1542744173-8e7e53415bb0?w=500", 0.0, "USD", false, false, "Dev Community")
        )

        mocks.forEach { event ->
            database.eventDatabaseQueries.insertEvent(
                event.id, event.title, event.category.name, event.location, event.dateTime.toString(),
                event.imageUrl, event.price, event.currency, event.isSaved, event.isRegistered, event.organizer
            )
        }
    }
}