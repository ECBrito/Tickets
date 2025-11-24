package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.UserProfile
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi

class EditProfileViewModel(
    private val repository: EventRepository,
    private val userId: String
) : ViewModel() {

    @OptIn(InternalSerializationApi::class)
    private val _profile = MutableStateFlow(UserProfile())
    @OptIn(InternalSerializationApi::class)
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProfile()
    }

    @OptIn(InternalSerializationApi::class)
    private fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            val user = repository.getUserProfile(userId)
            if (user != null) {
                _profile.value = user
            }
            _isLoading.value = false
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun saveProfile(
        name: String,
        bio: String,
        socialLink: String,
        newImageBytes: ByteArray?,
        isPublic: Boolean, // <--- Faltava este campo para a privacidade
        onSuccess: () -> Unit // <--- CORREÇÃO: Deve ser uma função, não um Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            var finalImageUrl = _profile.value.photoUrl

            // 1. Se houver nova imagem, faz upload
            if (newImageBytes != null) {
                val url = repository.uploadProfileImage(newImageBytes, userId)
                if (url != null) finalImageUrl = url
            }

            // 2. Atualiza objeto
            val updatedProfile = _profile.value.copy(
                name = name,
                bio = bio,
                socialLink = socialLink,
                photoUrl = finalImageUrl,
                isPublic = isPublic // <--- Guarda a escolha de privacidade
            )

            // 3. Grava na BD
            val success = repository.updateUserProfile(userId, updatedProfile)

            _isLoading.value = false

            // Se gravou com sucesso, chama o callback para voltar atrás
            if (success) {
                onSuccess()
            }
        }
    }
}