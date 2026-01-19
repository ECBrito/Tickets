package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.Event
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class) // <-- ESTA LINHA RESOLVE OS TEUS ERROS
@Composable
fun MyEvents(
    userId: String,
    onTicketClick: (String, String) -> Unit,
    onFavoriteClick: (String) -> Unit
) {
    val viewModel = remember { AppModule.provideMyEventsViewModel() }

    // Observamos 'registeredEvents' porque o teu ViewModel converte Tickets para Events
    val registeredEventsList by viewModel.registeredEvents.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Próximos", "Histórico")
    val now = Clock.System.now()

    val filteredEvents = remember(registeredEventsList, selectedTab) {
        val hoje = Clock.System.todayIn(TimeZone.currentSystemDefault())

        registeredEventsList.filter { event ->
            val dataDoEvento = try {
                // 1. Limpa espaços e pega apenas na parte da data (YYYY-MM-DD)
                val rawDate = event.dateTime.trim().split("T").first()
                LocalDate.parse(rawDate)
            } catch (e: Exception) {
                // Se falhar, coloca no passado (1970) para indicar erro
                LocalDate(1970, 1, 1)
            }

            // Comparação direta de datas (dia/mês/ano)
            if (selectedTab == 0) {
                dataDoEvento >= hoje // Futuro ou Hoje -> Próximos
            } else {
                dataDoEvento < hoje  // Passado -> Histórico
            }
        }
    }

    Scaffold(
        containerColor = Color(0xFF0B0A12),
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF0B0A12))) {
                Text(
                    text = "Os Meus Bilhetes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(24.dp)
                )
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF7B61FF),
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xFF7B61FF)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontSize = 16.sp) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFF7B61FF))
            } else if (filteredEvents.isEmpty()) {
                EmptyState(selectedTab)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredEvents) { event ->
                        TicketCard(
                            title = event.title,
                            date = event.dateTime.split("T").first(),
                            location = event.locationName,
                            imageUrl = event.imageUrl,
                            onClick = { onTicketClick(event.id, event.title) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TicketCard(title: String, date: String, location: String, imageUrl: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151520))
    ) {
        Row(modifier = Modifier.height(110.dp)) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.width(100.dp).fillMaxHeight()
            )
            Column(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(location, color = Color.Gray, fontSize = 12.sp, maxLines = 1)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(date, color = Color(0xFFD0BCFF), fontSize = 13.sp)
                    Icon(Icons.Default.QrCode, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun EmptyState(tabIndex: Int) {
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(if (tabIndex == 0) Icons.Default.ConfirmationNumber else Icons.Default.EventBusy, null, tint = Color.Gray.copy(alpha = 0.3f), modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(text = if (tabIndex == 0) "Sem bilhetes ativos." else "Histórico vazio.", color = Color.Gray)
    }
}