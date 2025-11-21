package com.example.eventify.model

enum class Category { MUSIC, SPORTS, THEATER, OTHER }



data class Event(
    val id: String,
    val title: String,
    val description: String,
    val location: String,
    val imageUrl: String,
    val dateTime: String,
    val category: Category,
    val registeredUserIds: List<String> = emptyList(), // <-- adicionar
    val isRegistered: Boolean = false
)
