package com.example.eventify.model

import kotlinx.serialization.Serializable

// 1. Enum para o Preço
@Serializable
enum class PriceType {
    ANY,
    FREE,
    PAID
}

// 2. Enum para Categorias (Estava em falta!)
@Serializable
enum class EventCategory {
    MUSIC,
    SPORTS,
    ARTS,
    FOOD,
    TECH,
    WORKSHOP,
    OTHER
}

// 3. O Estado dos Filtros
@Serializable
data class FilterState(
    val dateFrom: String? = null,
    val dateTo: String? = null,
    val locationRadiusKm: Int = 50,
    val useCurrentLocation: Boolean = true,
    // Agora o EventCategory já existe aqui, por isso não dá erro
    val categories: Set<EventCategory> = emptySet(),
    val priceType: PriceType = PriceType.ANY
)