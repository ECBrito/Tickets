 package com.example.eventify.model
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
enum class Category { MUSIC, SPORTS, THEATER, OTHER }

 @InternalSerializationApi
 @Serializable
 data class Event(
     val id: String = "",
     val title: String = "",
     val description: String = "",
     val locationName: String = "", // Nome do local (ex: Estádio da Luz)
     val imageUrl: String = "",
     val dateTime: String = "",
     val endDateTime: String = "",
     val category: String = "",
     val registeredUserIds: List<String> = emptyList(),
     val isRegistered: Boolean = false,
     val isSaved: Boolean = false,
     val price: Double = 0.0,
     val shares: Int = 0,
     val maxCapacity: Int = 100,
     val organizerId: String = "",
     val rating: Double = 0.0,
     val reviewCount: Int = 0,
     val latitude: Double = 0.0,
     val longitude: Double = 0.0,
     val isFeatured: Boolean = false
 )