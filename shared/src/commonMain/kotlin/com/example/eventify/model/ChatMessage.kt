 package com.example.eventify.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

 @InternalSerializationApi
@Serializable
data class ChatMessage(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val text: String = "",
    val timestamp: Long = 0
)