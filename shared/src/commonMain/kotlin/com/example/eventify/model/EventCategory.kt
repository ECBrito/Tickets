package com.example.eventify.model

import kotlinx.serialization.Serializable

@Serializable
enum class EventCategory {
    MUSIC,
    SPORTS,
    TECHNOLOGY,
    ART,
    OTHER,
    CONCERT,      // Adicionado para compatibilidade com o teu código anterior
    CONFERENCE,   // Adicionado para compatibilidade
    WORKSHOP,     // Adicionado para compatibilidade
    FESTIVAL      // Adicionado para compatibilidade
}