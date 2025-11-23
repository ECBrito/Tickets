package com.example.eventify.model

enum class TicketValidationResult {
    VALID,        // Bilhete válido e entrada autorizada
    ALREADY_USED, // Bilhete já entrou anteriormente
    INVALID,      // Bilhete não existe na base de dados
    ERROR         // Erro de rede ou outro
}