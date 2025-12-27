package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Event
import com.example.eventify.model.EventCategory
import com.example.eventify.repository.EventRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.InternalSerializationApi
import kotlin.math.*

@OptIn(InternalSerializationApi::class)
class HomeViewModelKMM(
    private val repository: EventRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedCategory = MutableStateFlow<EventCategory?>(null)
    val selectedCategory: StateFlow<EventCategory?> = _selectedCategory

    private val _userInterests = MutableStateFlow<List<String>>(emptyList())

    // Coordenadas guardadas (Privadas para o ViewModel)
    val userLat = MutableStateFlow<Double?>(null)
    val userLon = MutableStateFlow<Double?>(null)

    private val currentUserId = Firebase.auth.currentUser?.uid ?: ""

    // --- FLOWS BASE ---
    private val allEventsFlow = repository.events

    private val favoritesFlow = if (currentUserId.isNotEmpty()) {
        repository.getFavoriteEventIds(currentUserId)
    } else {
        flowOf(emptyList())
    }

    private val eventsWithFavs = combine(allEventsFlow, favoritesFlow) { events, favIds ->
        events.map { event ->
            event.copy(isSaved = favIds.contains(event.id))
        }.sortedBy { it.dateTime }
    }

    // --- LISTAS PARA A UI ---

    // 1. Featured
    val featuredEvents: StateFlow<List<Event>> = eventsWithFavs
        .map { events ->
            events.filter { it.isFeatured }.take(5)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Upcoming (Com lógica de proximidade reativa)
    val upcomingEvents: StateFlow<List<Event>> = combine(
        eventsWithFavs,
        _selectedCategory,
        userLat,
        userLon
    ) { events, category, lat, lon ->
        var filtered = events

        if (category != null) {
            filtered = filtered.filter { it.category.equals(category.name, ignoreCase = true) }
        }

        if (lat != null && lon != null) {
            filtered = filtered.filter { event ->
                val distance = calculateDistance(lat, lon, event.latitude, event.longitude)
                distance <= 50.0 // Filtro de 50km
            }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. For You
    val forYouEvents: StateFlow<List<Event>> = combine(
        eventsWithFavs,
        _userInterests
    ) { events, interests ->
        if (interests.isEmpty()) emptyList()
        else {
            events.filter { event ->
                interests.any { it.equals(event.category, ignoreCase = true) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            if (currentUserId.isNotBlank()) {
                repository.getUserProfile(currentUserId)?.let { profile ->
                    _userInterests.value = profile.interests
                }
            }
            eventsWithFavs.collect { _isLoading.value = false }
        }
    }

    // --- A FUNÇÃO QUE FALTAVA ---
    // Esta função é chamada pela HomeScreen para injetar o GPS no ViewModel
    fun updateUserLocation(lat: Double, lon: Double) {
        userLat.value = lat
        userLon.value = lon
    }

    // --- AÇÕES ---

    fun selectCategory(category: EventCategory?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun toggleSave(eventId: String) {
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            repository.toggleFavorite(currentUserId, eventId)
        }
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371
        val dLat = (lat2 - lat1).toRadians()
        val dLon = (lon2 - lon1).toRadians()
        val a = sin(dLat / 2).pow(2) +
                cos(lat1.toRadians()) * cos(lat2.toRadians()) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    private fun Double.toRadians() = this * PI / 180.0
}