package com.example.eventify.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val socialLink: String = "",
    val role: String = "user" // user ou organizer
)