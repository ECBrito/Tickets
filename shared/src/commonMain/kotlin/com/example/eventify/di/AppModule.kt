package com.example.eventify.di

import com.example.eventify.repository.EventRepository
import com.example.eventify.repository.EventRepositoryImpl
import com.example.eventify.viewmodels.* // <--- Isto importa todos os ViewModels (Home, Explore, Organizer, etc.)
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

object AppModule {

    // 1. Inicializa o Firestore
    private val firestore = Firebase.firestore

    // 2. Cria o Repositório Único
    val eventRepository: EventRepository by lazy {
        EventRepositoryImpl(firestore)
    }

    // 3. Mock User ID
    private const val CURRENT_USER_ID = "user_123_mock"

    // ------------------------------------------------------
    //  PROVIDERS (Fábricas de ViewModels)
    // ------------------------------------------------------

    fun provideHomeViewModel(): HomeViewModelKMM {
        return HomeViewModelKMM(eventRepository)
    }

    fun provideExploreViewModel(): ExploreViewModelKMM {
        return ExploreViewModelKMM(eventRepository)
    }

    fun provideMyEventsViewModel(): MyEventsViewModelKMM {
        return MyEventsViewModelKMM(eventRepository, CURRENT_USER_ID)
    }

    fun provideEventDetailViewModel(eventId: String): EventDetailViewModelKMM {
        return EventDetailViewModelKMM(eventRepository, eventId, CURRENT_USER_ID)
    }

    fun provideCreateEventViewModel(): CreateEventViewModel {
        return CreateEventViewModel(eventRepository, CURRENT_USER_ID)
    }

    // --- A FUNÇÃO QUE ESTAVA A FALTAR ---
    fun provideOrganizerViewModel(): OrganizerViewModel {
        return OrganizerViewModel(eventRepository)
    }
}