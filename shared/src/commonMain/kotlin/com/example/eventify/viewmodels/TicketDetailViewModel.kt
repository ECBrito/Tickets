package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TicketDetailViewModel(
    private val repository: EventRepository,
    private val ticketId: String,
    private val currentUserId: String
) : ViewModel() {

    private val _transferStatus = MutableStateFlow<Result?>(null)
    val transferStatus: StateFlow<Result?> = _transferStatus.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    enum class Result { SUCCESS, ERROR, USER_NOT_FOUND }

    fun transferTicket(email: String) {
        if (email.isBlank()) return

        viewModelScope.launch {
            _isLoading.value = true
            val success = repository.transferTicket(ticketId, currentUserId, email.trim())
            _isLoading.value = false

            _transferStatus.value = if (success) Result.SUCCESS else Result.ERROR
        }
    }

    fun resetStatus() {
        _transferStatus.value = null
    }
}