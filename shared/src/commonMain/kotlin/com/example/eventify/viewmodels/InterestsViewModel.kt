package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.EventCategory
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi

class InterestsViewModel(
    private val repository: EventRepository,
    private val userId: String
) : ViewModel() {

    // Lista de interesses selecionados
    private val _selectedInterests = MutableStateFlow<Set<String>>(emptySet())
    val selectedInterests: StateFlow<Set<String>> = _selectedInterests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadCurrentInterests()
    }

    @OptIn(InternalSerializationApi::class)
    private fun loadCurrentInterests() {
        viewModelScope.launch {
            _isLoading.value = true
            val profile = repository.getUserProfile(userId)
            if (profile != null) {
                _selectedInterests.value = profile.interests.toSet()
            }
            _isLoading.value = false
        }
    }

    fun toggleInterest(category: EventCategory) {
        val current = _selectedInterests.value.toMutableSet()
        val catName = category.name

        if (current.contains(catName)) {
            current.remove(catName)
        } else {
            current.add(catName)
        }
        _selectedInterests.value = current
    }

    @OptIn(InternalSerializationApi::class)
    fun saveInterests(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val currentProfile = repository.getUserProfile(userId)

            if (currentProfile != null) {
                val updatedProfile = currentProfile.copy(
                    interests = _selectedInterests.value.toList()
                )
                repository.updateUserProfile(userId, updatedProfile)
                onSuccess()
            }
            _isLoading.value = false
        }
    }
}