package com.example.eventify.model

import kotlinx.serialization.Serializable

@Serializable
data class Comment(
    val id: String = "",
    val userId: String = "",
    val userName: String = "", // Guardamos o nome para não ter de buscar o user a cada comentário
    val userPhotoUrl: String? = null,
    val text: String = "",
    val timestamp: Long = 0
)