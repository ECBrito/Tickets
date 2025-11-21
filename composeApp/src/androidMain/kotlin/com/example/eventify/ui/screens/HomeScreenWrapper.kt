package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.eventify.model.Event
import com.example.eventify.repository.EventRepositoryImplKMM
import com.example.eventify.ui.components.EventCard

@Composable
fun HomeScreenWrapper(navController: NavHostController) {
    val bottomNavController = rememberNavController()
    val repository = remember { EventRepositoryImplKMM() }

    val featuredEvents by repository.events.collectAsState()
    val upcomingEvents by repository.events.collectAsState()
    val isLoading = false

    Scaffold(
        bottomBar = { /* teu EventifyBottomBar */ },
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { Spacer(Modifier.height(16.dp)) }
                    items(featuredEvents) { event ->
                        EventCard(event = event, onClick = {}, onSave = {})
                    }
                }
            }
        }
    }
}
