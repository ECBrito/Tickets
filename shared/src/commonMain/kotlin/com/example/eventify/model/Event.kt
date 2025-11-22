package com.example.eventify.model

enum class Category { MUSIC, SPORTS, THEATER, OTHER }



data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val dateTime: String = "",
    val category: String = "",
    val registeredUsers: List<String> = emptyList(),
    val isRegistered: Boolean = false,
    val isSaved: Boolean = false,
    val registeredUserIds: List<String> = emptyList(),
    val price: Double = 0.0
)