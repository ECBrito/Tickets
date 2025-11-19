package com.example.eventify.model
import kotlinx.datetime.LocalDateTime

/**
 * An enum to represent the different categories of events in a type-safe way.
 * Using an enum prevents typos and makes the code easier to read and maintain.
 */
enum class EventCategory {
    CONCERT,
    CONFERENCE,
    SPORTS,
    WORKSHOP,
    FESTIVAL,
    OTHER
}

/**
 * Data class to model an event.
 *
 * It has been improved to use more appropriate data types for price and category,
 * making the application more robust and less error-prone.
 */
data class Event(
    val id: String,
    val title: String,
    val category: EventCategory, // <-- CHANGED: Now uses the type-safe enum
    val location: String,
    val dateTime: LocalDateTime,
    val imageUrl: String,
    val price: Double,          // <-- CHANGED: Use Double for calculations
    val currency: String,       // <-- ADDED: To know the currency (e.g., "USD", "EUR")
    val isSaved: Boolean,
    val isRegistered: Boolean,
    val organizer: String
)