 package com.example.eventify.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

 @InternalSerializationApi
@Serializable
data class PromoCode(
    val code: String = "",        // Ex: "SUMMER20"
    val discountPercent: Int = 0, // Ex: 20 (significa 20%)
    val isActive: Boolean = true  // Se o código ainda é válido
)