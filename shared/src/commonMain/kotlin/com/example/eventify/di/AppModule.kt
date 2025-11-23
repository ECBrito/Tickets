package com.example.eventify.di

import com.example.eventify.repository.EventRepository
import com.example.eventify.repository.EventRepositoryImpl
import com.example.eventify.viewmodels.*
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth // <--- Importante
import dev.gitlive.firebase.firestore.firestore
import dev.gitlive.firebase.storage.storage

object AppModule {

    private val firestore = Firebase.firestore
    private val storage = Firebase.storage
    private val auth = Firebase.auth // <--- Inicializa o Auth

    val eventRepository: EventRepository by lazy {
        EventRepositoryImpl(firestore, storage)
    }

    // --- CORREÇÃO CRÍTICA: Getter Dinâmico ---
    // Em vez de uma constante fixa, criamos uma propriedade que vai ler
    // o utilizador atual no momento em que é chamada.
    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    // --- PROVIDERS ---

    fun provideHomeViewModel(): HomeViewModelKMM {
        return HomeViewModelKMM(eventRepository)
    }

    fun provideExploreViewModel(): ExploreViewModelKMM {
        return ExploreViewModelKMM(eventRepository)
    }

    fun provideMyEventsViewModel(): MyEventsViewModelKMM {
        // Agora passa o ID real do user logado
        return MyEventsViewModelKMM(eventRepository, currentUserId)
    }

    fun provideEventDetailViewModel(eventId: String): EventDetailViewModelKMM {
        // Agora passa o ID real para verificar se já comprou
        return EventDetailViewModelKMM(eventRepository, eventId, currentUserId)
    }

    fun provideCreateEventViewModel(): CreateEventViewModel {
        return CreateEventViewModel(eventRepository, currentUserId)
    }

    fun provideOrganizerViewModel(): OrganizerViewModel {
        return OrganizerViewModel(eventRepository)
    }

    fun providePurchaseViewModel(eventId: String): PurchaseViewModel {
        // Aqui estava o erro! Agora passamos o ID real para a compra
        return PurchaseViewModel(eventRepository, currentUserId, eventId)
    }

    fun provideOrganizerEventDashboardViewModel(eventId: String): OrganizerEventDashboardViewModel {
        return OrganizerEventDashboardViewModel(eventRepository, eventId)
    }
}