 package com.example.eventify.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable

 @InternalSerializationApi
@Serializable
data class NotificationItem( // Usei NotificationItem para não confundir com a classe nativa Android
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = 0,
    val isRead: Boolean = false,
    val type: String = "info" // info, alert, success
)