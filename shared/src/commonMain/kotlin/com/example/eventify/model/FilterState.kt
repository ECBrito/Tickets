package com.example.eventify.model

import kotlinx.datetime.LocalDate

enum class PriceType { FREE, PAID, ALL }
enum class SortOption { RELEVANCE, NEAREST, NEWEST }

data class FilterState(
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val locationRadiusKm: Int = 10,
    val useCurrentLocation: Boolean = false,
    // Agora usa o EventCategory importado do mesmo pacote
    val categories: Set<EventCategory> = emptySet(),
    val priceType: PriceType = PriceType.ALL,
    val sortBy: SortOption = SortOption.RELEVANCE
)