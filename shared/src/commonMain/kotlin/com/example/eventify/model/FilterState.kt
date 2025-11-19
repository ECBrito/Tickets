package com.example.eventify.model

import kotlinx.datetime.LocalDate

/**
 * Enums de suporte para a filtragem.
 * Definimos aqui para que sejam acessíveis tanto no modelo quanto na UI.
 */
enum class PriceType { FREE, PAID, ALL }

enum class SortOption { RELEVANCE, NEAREST, NEWEST }

/**
 * Data class para armazenar o estado de todos os filtros da tela Explore.
 * Mantém o estado imutável para ser usado no fluxo de dados do KMM.
 */
data class FilterState(
    // Tipos corrigidos para KMM (usando kotlinx.datetime)
    val dateFrom: LocalDate? = null,
    val dateTo: LocalDate? = null,

    val locationRadiusKm: Int = 10, // Raio padrão de 10km
    val useCurrentLocation: Boolean = false,
    val categories: Set<EventCategory> = emptySet(), // Multi-select Chips
    val priceType: PriceType = PriceType.ALL,
    val sortBy: SortOption = SortOption.RELEVANCE
)