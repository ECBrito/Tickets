package com.example.eventify.model

data class Attendee(
    val ticketId: String,
    val userId: String,
    val name: String,
    val email: String,
    val photoUrl: String,
    val isCheckedIn: Boolean // Se ticket.isValid == false, então Check-in Feito (true)
)