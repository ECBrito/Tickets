 package com.example.eventify.model

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
 @InternalSerializationApi
@Serializable
data class Attendee(
    val ticketId: String,
    val userId: String,
    val name: String,
    val email: String,
    val photoUrl: String,
    val isCheckedIn: Boolean,
    val isPublic: Boolean = true // <--- ESTE CAMPO É OBRIGATÓRIO PARA CORRIGIR O ERRO
)