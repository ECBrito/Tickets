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
        imageUrl: String?, // URL local (apenas para compatibilidade, será ignorado se houver bytes)
        imageBytes: ByteArray?, // <--- A IMAGEM REAL (BYTES)
        dateTime: String,
        category: String,
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

                // --- PASSO 1: UPLOAD DA IMAGEM (Se existir) ---
                if (imageBytes != null) {
                    // Gera um nome único baseado no tempo atual
                    val fileName = "${Clock.System.now().toEpochMilliseconds()}.jpg"

                    // Faz o upload e recebe o link público (https://...)
                    val uploadedUrl = repository.uploadEventImage(imageBytes, fileName)

                    if (uploadedUrl != null) {
                        finalImageUrl = uploadedUrl
                    } else {
                        println("Aviso: Upload da imagem falhou, criando evento sem imagem.")
                    }
                }

                // --- PASSO 2: CRIAR O OBJETO EVENTO ---
                val event = Event(
                    id = "", // O Firebase gera o ID
                    title = title,
                    description = description,
                    location = location,
                    imageUrl = finalImageUrl, // Usa o link da nuvem
                    dateTime = dateTime,
                    category = category,
                    registeredUserIds = listOf(organizerId), // O criador vai automaticamente
                    isRegistered = true
                )

                // --- PASSO 3: GRAVAR NA BASE DE DADOS ---
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