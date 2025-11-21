package com.example.eventify.model

import kotlinx.serialization.Serializable

@Serializable
enum class EventCategory {
    MUSIC,
    SPORTS,
    TECHNOLOGY,
    ART,
    FOOD,
    BUSINESS,
    WORKSHOP,
    CONCERT,
    CONFERENCE,
    FESTIVAL,
    OTHER;

    companion object {
        // Helper para converter string em categoria sem crashar
        fun fromString(value: String): EventCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHER
        }
    }
}