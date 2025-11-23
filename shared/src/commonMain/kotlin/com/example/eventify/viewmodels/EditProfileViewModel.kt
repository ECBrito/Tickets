package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.UserProfile
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val repository: EventRepository,
    private val userId: String
) : ViewModel() {

    private val _profile = MutableStateFlow(UserProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadProfile()
    }

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

    fun saveProfile(
        name: String,
        bio: String,
        socialLink: String,
        newImageBytes: ByteArray?,
        onSuccess: () -> Unit
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
                photoUrl = finalImageUrl
            )

            // 3. Grava na BD
            val success = repository.updateUserProfile(userId, updatedProfile)

            _isLoading.value = false
            if (success) onSuccess()
        }
    }
}