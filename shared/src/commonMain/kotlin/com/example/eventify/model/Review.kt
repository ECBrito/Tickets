 package com.example.eventify.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

 @InternalSerializationApi
@Serializable
data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val rating: Int = 0, // 1 a 5
    val comment: String = "",
    val timestamp: Long = 0
)