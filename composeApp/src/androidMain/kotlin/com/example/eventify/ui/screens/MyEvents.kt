package com.example.eventify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.eventify.di.AppModule
import com.example.eventify.ui.components.EventCard
import kotlinx.serialization.InternalSerializationApi

// Cores
private val BgDark = Color(0xFF0B0A12)
private val AccentPurple = Color(0xFF7B61FF)

@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun MyEvents(
    userId: String,
    // NOVOS PARÂMETROS PARA A NAVEGAÇÃO FUNCIONAR:
    onTicketClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    val viewModel = remember { AppModule.provideMyEventsViewModel() }

    val registeredEvents by viewModel.registeredEvents.collectAsState()
    val favoriteEvents by viewModel.favoriteEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Estado das Abas (0 = Tickets, 1 = Favorites)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("My Tickets", "Favorites")

    Scaffold(
        containerColor = BgDark,
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Text("My Events", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = BgDark)
                )

                // --- ABAS ---
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = BgDark,
                    contentColor = AccentPurple,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = AccentPurple
                        )
                    },
                    divider = { HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f)) }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) Color.White else Color.Gray
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentPurple)
                }
            } else {
                // Decide qual lista mostrar
                val listToShow = if (selectedTabIndex == 0) registeredEvents else favoriteEvents
                val emptyMessage = if (selectedTabIndex == 0) "No tickets yet." else "No favorites yet."

                if (listToShow.isEmpty()) {
                    EmptyState(message = emptyMessage)
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(listToShow) { event ->
                            EventCard(
                                event = event,
                                onClick = {
                                    // LÓGICA DE CLIQUE:
                                    if (selectedTabIndex == 0) {
                                        onTicketClick(event.id) // Abre QR Code
                                    } else {
                                        onFavoriteClick(event.id) // Abre Detalhes para comprar
                                    }
                                },
                                onSave = {
                                    // Só permite remover favoritos na aba de favoritos
                                    if (selectedTabIndex == 1) {
                                        viewModel.removeFavorite(event.id)
                                    }
                                }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ConfirmationNumber,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Go explore events!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}