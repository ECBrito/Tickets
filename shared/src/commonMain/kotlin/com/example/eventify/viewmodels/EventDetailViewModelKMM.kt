package com.example.eventify.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.eventify.model.Attendee
import com.example.eventify.model.Comment
import com.example.eventify.model.ChatMessage
import com.example.eventify.model.Event
import com.example.eventify.model.Review
import com.example.eventify.repository.EventRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.InternalSerializationApi

class EventDetailViewModelKMM(
    private val repository: EventRepository,
    private val eventId: String,
    private val userId: String
) : ViewModel() {

    @OptIn(InternalSerializationApi::class)
    private val _event = MutableStateFlow<Event?>(null)
    @OptIn(InternalSerializationApi::class)
    val event: StateFlow<Event?> = _event.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    @OptIn(InternalSerializationApi::class)
    private val _attendees = MutableStateFlow<List<Attendee>>(emptyList())
    @OptIn(InternalSerializationApi::class)
    val attendees: StateFlow<List<Attendee>> = _attendees.asStateFlow()

    @OptIn(InternalSerializationApi::class)
    private val _reviews = MutableStateFlow<List<Review>>(emptyList())
    @OptIn(InternalSerializationApi::class)
    val reviews: StateFlow<List<Review>> = _reviews.asStateFlow()

    @OptIn(InternalSerializationApi::class)
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    @OptIn(InternalSerializationApi::class)
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isFollowingOrganizer = MutableStateFlow(false)
    val isFollowingOrganizer: StateFlow<Boolean> = _isFollowingOrganizer.asStateFlow()

    init {
        observeEventAndTicket() // <--- Lógica nova aqui
        observeComments()
        observeChat()
        observeReviews()
        loadAttendees()
    }

    // Combina a leitura do Evento com a verificação do Bilhete
    @OptIn(InternalSerializationApi::class)
    private fun observeEventAndTicket() {
        viewModelScope.launch {
            _isLoading.value = true

            // 1. Flow dos Eventos
            val eventFlow = repository.events

            // 2. Flow do Bilhete (Verifica se tenho bilhete para ESTE evento)
            val ticketFlow = repository.hasTicketFlow(userId, eventId)

            // Combina os dois
            combine(eventFlow, ticketFlow) { eventsList, hasTicket ->
                val foundEvent = eventsList.find { it.id == eventId }

                // Aqui forçamos o isRegistered a ser igual ao hasTicket (da coleção tickets)
                // e ignoramos o array antigo registeredUserIds
                foundEvent?.copy(isRegistered = hasTicket)

            }.collect { eventWithStatus ->
                _event.value = eventWithStatus

                // Verifica follow status se o evento carregou
                if (eventWithStatus != null && eventWithStatus.organizerId.isNotBlank()) {
                    checkFollowStatus(eventWithStatus.organizerId)
                }

                _isLoading.value = false
            }
        }
    }

    // ... (Resto das funções iguais) ...

    private fun checkFollowStatus(organizerId: String) {
        viewModelScope.launch {
            repository.isFollowing(userId, organizerId).collectLatest { _isFollowingOrganizer.value = it }
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun toggleFollow() {
        val organizerId = _event.value?.organizerId ?: return
        viewModelScope.launch { repository.toggleFollow(userId, organizerId) }
    }

    private fun observeComments() {
        viewModelScope.launch { repository.getComments(eventId).collect { _comments.value = it } }
    }

    @OptIn(InternalSerializationApi::class)
    private fun observeChat() {
        viewModelScope.launch { repository.getChatMessages(eventId).collect { _chatMessages.value = it } }
    }

    @OptIn(InternalSerializationApi::class)
    private fun observeReviews() {
        viewModelScope.launch { repository.getReviews(eventId).collect { _reviews.value = it } }
    }

    @OptIn(InternalSerializationApi::class)
    private fun loadAttendees() {
        viewModelScope.launch { _attendees.value = repository.getEventAttendees(eventId) }
    }

    fun toggleRsvp() {
        // Nota: Como agora usamos bilhetes pagos, o "Toggle" para cancelar
        // deve ser tratado como reembolso ou simplesmente esconder o bilhete.
        // Para simplificar, mantemos a chamada antiga mas ela não vai apagar o bilhete da coleção 'tickets'.
        // Se quiseres permitir "devolver" o bilhete, terias de criar um deleteTicket no repo.
        println("Cancelar bilhete ainda não implementado na UI de detalhes.")
    }

    fun sendComment(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val currentUser = Firebase.auth.currentUser
            val userName = currentUser?.displayName ?: "User"
            val newComment = Comment(userId = userId, userName = userName, text = text, timestamp = Clock.System.now().toEpochMilliseconds(), userPhotoUrl = currentUser?.photoURL)
            repository.addComment(eventId, newComment)
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun sendMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val currentUser = Firebase.auth.currentUser
            val userName = currentUser?.displayName ?: "User"
            val msg = ChatMessage(userId = userId, userName = userName, text = text, timestamp = Clock.System.now().toEpochMilliseconds(), userPhotoUrl = currentUser?.photoURL)
            repository.sendChatMessage(eventId, msg)
        }
    }

    @OptIn(InternalSerializationApi::class)
    fun submitReview(rating: Int, comment: String) {
        viewModelScope.launch {
            val currentUser = Firebase.auth.currentUser
            val userName = currentUser?.displayName ?: "User"
            val review = Review(userId = userId, userName = userName, userPhotoUrl = currentUser?.photoURL, rating = rating, comment = comment, timestamp = Clock.System.now().toEpochMilliseconds())
            repository.addReview(eventId, review)
        }
    }

    fun registerShare() { viewModelScope.launch { repository.incrementEventShares(eventId) } }

    @OptIn(InternalSerializationApi::class)
    val isRegistered: Boolean get() = _event.value?.isRegistered == true
}