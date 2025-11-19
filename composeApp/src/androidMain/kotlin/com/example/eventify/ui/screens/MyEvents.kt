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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventify.ui.components.EventCard
import com.example.eventify.ui.theme.EventifyTheme
import com.example.eventify.ui.viewmodels.MyEventsViewModel

@OptIn(ExperimentalMaterial3Api::class) // <-- CORREÇÃO AQUI: Permite o uso de APIs experimentais M3
@Composable
fun MyEventsScreen(
    onEventClick: (String) -> Unit,
    viewModel: MyEventsViewModel = viewModel() // Injeção do ViewModel
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Registered", "Hosted")

    // Observar os estados do ViewModel
    val registeredEvents by viewModel.registeredEvents.collectAsState()
    val hostedEvents by viewModel.hostedEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Top Bar Simples
        CenterAlignedTopAppBar(
            title = {
                Text(
                    "My Events",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.onBackground
            )
        )

        // 2. Abas (Tabs)
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.PrimaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Conteúdo da Lista
        // Decide qual lista mostrar com base na aba
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
                    EventCard(
                        event = event,
                        onClick = { onEventClick(event.id) },
                        onSave = { /* Lógica de bookmark futura */ }
                    )
                }
                // Espaço extra no fundo para não ficar atrás da BottomNav
                item { Spacer(modifier = Modifier.height(80.dp)) }
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
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview
@Composable
fun MyEventsScreenPreview() {
    EventifyTheme(darkTheme = true) {
        MyEventsScreen(onEventClick = {})
    }
}