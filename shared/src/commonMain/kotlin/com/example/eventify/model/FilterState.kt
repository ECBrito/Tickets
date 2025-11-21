package com.example.eventify.model

import kotlinx.datetime.LocalDate

// Se PriceType e SortOption não estiverem noutro lado, mantemos aqui
enum class PriceType { FREE, PAID, ALL }
enum class SortOption { RELEVANCE, NEAREST, NEWEST }

data class FilterState(
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,
    val locationRadiusKm: Int = 10,
    val useCurrentLocation: Boolean = false,
    // Usa o EventCategory que criámos no passo 1 (está no mesmo pacote, não precisa de import se package for igual)
    val categories: Set<EventCategory> = emptySet(),
    val priceType: PriceType = PriceType.ALL,
    val sortBy: SortOption = SortOption.RELEVANCE
)