package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Badge
import com.example.eventify.model.UserProfile
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi

class ProfileViewModel(
    private val repository: EventRepository,
    private val userId: String
) : ViewModel() {

    @OptIn(InternalSerializationApi::class)
    private val _profile = MutableStateFlow(UserProfile())
    @OptIn(InternalSerializationApi::class)
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    val badges: StateFlow<List<Badge>> = _badges.asStateFlow()

    init {
        loadProfileData()
    }

    @OptIn(InternalSerializationApi::class)
    private fun loadProfileData() {
        viewModelScope.launch {
            // 1. Perfil
            val user = repository.getUserProfile(userId)
            if (user != null) _profile.value = user

            // 2. Calcular Badges
            val ticketCount = repository.getUserTicketCount(userId)
            val commentCount = repository.getUserCommentCount(userId)

            val earnedBadges = mutableListOf<Badge>()

            // Regras de Negócio:
            if (ticketCount >= 1) earnedBadges.add(Badge.ROOKIE)
            if (commentCount >= 5) earnedBadges.add(Badge.SOCIAL)
            if (ticketCount >= 5) earnedBadges.add(Badge.VIP)

            _badges.value = earnedBadges
        }
    }
}