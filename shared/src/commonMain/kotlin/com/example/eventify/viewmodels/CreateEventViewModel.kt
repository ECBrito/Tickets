package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.InternalSerializationApi

class CreateEventViewModel(
    private val repository: EventRepository,
    private val organizerId: String
) : ViewModel() {

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    @OptIn(InternalSerializationApi::class)
    fun createEvent(
        title: String,
        description: String,
        location: String,
        imageUrl: String?,
        imageBytes: ByteArray?, // Imagem Real
        dateTime: String,
        category: String,
        // --- NOVOS CAMPOS PARA O DASHBOARD ---
        price: Double = 0.0,     // Preço do bilhete (padrão 0 se não for passado)
        maxCapacity: Int = 100,  // Lotação máxima (padrão 100)
        // -------------------------------------
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (title.isBlank() || location.isBlank()) {
            onError?.invoke("Título e Localização são obrigatórios")
            return
        }

        _loading.value = true

        viewModelScope.launch {
            try {
                var finalImageUrl = ""

                // --- PASSO 1: UPLOAD DA IMAGEM ---
                if (imageBytes != null) {
                    val fileName = "${Clock.System.now().toEpochMilliseconds()}.jpg"
                    val uploadedUrl = repository.uploadEventImage(imageBytes, fileName)

                    if (uploadedUrl != null) {
                        finalImageUrl = uploadedUrl
                    } else {
                        println("Aviso: Upload da imagem falhou.")
                    }
                }

                // --- PASSO 2: CRIAR O OBJETO EVENTO ---
                val event = Event(
                    id = "", // Gerado pelo Firestore
                    title = title,
                    description = description,
                    location = location,
                    imageUrl = finalImageUrl,
                    dateTime = dateTime,
                    category = category,
                    organizerId = organizerId,
                    registeredUserIds = listOf(organizerId),
                    isRegistered = true,
                    // --- DADOS FINANCEIROS E ESTATÍSTICOS ---
                    price = price,
                    maxCapacity = maxCapacity,
                    shares = 0 // Inicia com 0 partilhas
                )

                // --- PASSO 3: GRAVAR ---
                val success = repository.addEvent(event)

                if (success) {
                    onSuccess?.invoke()
                } else {
                    onError?.invoke("Falha ao gravar evento na base de dados.")
                }

            } catch (e: Exception) {
                onError?.invoke(e.message ?: "Erro desconhecido durante a criação.")
            } finally {
                _loading.value = false
            }
        }
    }
}