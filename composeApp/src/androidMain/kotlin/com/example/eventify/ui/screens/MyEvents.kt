package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.eventify.di.AppModule
import com.example.eventify.ui.components.EventCard // O teu componente rico

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyEvents( // O nome deve ser MyEvents para bater certo com o MainScreen
    userId: String, // Mantemos o param mas usamos o do AppModule internamente
    onEventClick: (String) -> Unit
) {
    // 1. INJEÇÃO NOVA: Usar AppModule
    val viewModel = remember { AppModule.provideMyEventsViewModel() }

    // 2. ESTADOS DO VIEWMODEL
    val registeredEvents by viewModel.registeredEvents.collectAsState()
    val hostedEvents by viewModel.hostedEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 3. ESTADO DAS TABS
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Registered", "Hosted")

    Scaffold(
        containerColor = Color(0xFF0B0A12), // Fundo escuro
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "My Events",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0B0A12)
                )
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // --- TABS ---
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color(0xFF0B0A12),
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                divider = { HorizontalDivider(color = Color(0xFF151520)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- CONTEÚDO DA LISTA ---
            val eventsToShow = if (selectedTabIndex == 0) registeredEvents else hostedEvents

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (eventsToShow.isEmpty()) {
                EmptyState(tabName = tabs[selectedTabIndex])
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(eventsToShow) { event ->
                        // Usar o teu EventCard rico
                        EventCard(
                            event = event,
                            onClick = { onEventClick(event.id) },
                            onSave = { /* Lógica futura */ }
                        )
                    }
                    // Espaço extra no fundo para não ficar atrás da BottomNav
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun EmptyState(tabName: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No $tabName events found.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
        }
    }
}