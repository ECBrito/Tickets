package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.NotificationItem
import com.example.eventify.repository.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
class NotificationsViewModel(
    private val repository: EventRepository,
    private val userId: String
) : ViewModel() {

    @OptIn(InternalSerializationApi::class)
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    @OptIn(InternalSerializationApi::class)
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getUserNotifications(userId).collect {
                _notifications.value = it
            }
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            repository.markNotificationAsRead(userId, notificationId)
        }
    }

    // Botão mágico para testar
    fun generateTestNotification() {
        viewModelScope.launch {
            repository.createTestNotification(userId)
        }
    }
}