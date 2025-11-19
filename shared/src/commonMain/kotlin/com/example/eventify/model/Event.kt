package com.example.eventify.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Modelo de dados para um Evento.
 *
 * A anotação @Serializable é crucial para que o Firebase/Firestore (via KotlinX Serialization)
 * consiga converter este objeto em JSON e vice-versa, permitindo que seja guardado e lido da base de dados na nuvem.
 */
@Serializable
data class Event(
    // O ID é agora uma String, que é o formato padrão usado pelo Firestore.
    // Usar "" como valor predefinido para novos eventos antes de serem guardados.
    @SerialName("id")
    val id: String = "",
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String,
    // Manter a data como String por enquanto, mas seria melhor usar Instant (próximo passo).
    @SerialName("date")
    val date: String,
    @SerialName("location")
    val location: String,
    @SerialName("imageUrl")
    val imageUrl: String? = null // URL da imagem guardada no Firebase Storage
)

// Exemplo de uso
// val newEvent = Event(title = "Concerto", description = "Rock", date = "2024-12-31", location = "Lisboa")
// O ID será preenchido pelo Firestore após o upload.