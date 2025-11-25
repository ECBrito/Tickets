 package com.example.eventify.model
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
enum class Category { MUSIC, SPORTS, THEATER, OTHER }

@InternalSerializationApi
@Serializable // <--- TEM DE TER ESTA ANOTAÇÃO
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
    val price: Double = 0.0,
    val shares: Int = 0, // <--- NOVO
    val maxCapacity: Int = 100, // <--- NOVO CAMPO (Default 100).
    val organizerId: String = ""
)