package com.example.eventify.ui.screens.organizer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.eventify.di.AppModule
import com.example.eventify.viewmodels.EventStats
import com.example.eventify.viewmodels.OrganizerEventDashboardViewModel
import com.example.eventify.viewmodels.TimeRange
import java.text.NumberFormat
import java.util.Locale

// Cores do Tema da Imagem
private val DashboardBg = Color(0xFF0B0A12)
private val CardBg = Color(0xFF1E1E2C) // Um pouco mais claro que o fundo
private val AccentPurple = Color(0xFF6C5DD3) // Roxo do botão 7d e barra
private val TextWhite = Color.White
private val TextGray = Color(0xFF8F9BB3)
private val SuccessGreen = Color(0xFF00E096)
private val ErrorRed = Color(0xFFFF3D71)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerEventDashboard(
    eventId: String,
    navController: NavController
) {
    val viewModel = remember { AppModule.provideOrganizerEventDashboardViewModel(eventId) }
    val event by viewModel.event.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val selectedRange by viewModel.selectedTimeRange.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        containerColor = DashboardBg,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dashboard", color = TextWhite, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextWhite)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = DashboardBg)
            )
        }
    ) { innerPadding ->
        if (isLoading && event == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentPurple)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Título do Evento e Data
                Text(
                    text = event?.title ?: "Loading...",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event?.dateTime?.replace("T", " ") ?: "", // Simples format
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextGray
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Filtro de Tempo (Tab Row Customizada)
                TimeRangeSelector(
                    selectedRange = selectedRange,
                    onRangeSelected = { viewModel.setTimeRange(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Grid de Estatísticas

                // Linha 1: Attendees e Sales
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Total Attendees",
                        value = "${stats.totalAttendees}",
                        growth = stats.attendeesGrowth
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Ticket Sales",
                        value = formatCurrency(stats.ticketSales),
                        growth = stats.salesGrowth
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Linha 2: Capacity (Largo)
                CapacityCard(stats)

                Spacer(modifier = Modifier.height(16.dp))

                // Linha 3: Engagement e Shares
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Engagement",
                        value = "${stats.engagementScore}/10",
                        growth = stats.engagementGrowth
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        title = "Social Shares",
                        value = "${stats.socialShares}",
                        growth = stats.sharesGrowth
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

// --- COMPONENTES VISUAIS ---

@Composable
fun TimeRangeSelector(
    selectedRange: TimeRange,
    onRangeSelected: (TimeRange) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(CardBg, RoundedCornerShape(12.dp))
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimeRange.entries.forEach { range ->
            val isSelected = range == selectedRange
            val label = when (range) {
                TimeRange.HOURS_24 -> "24h"
                TimeRange.DAYS_7 -> "7d"
                TimeRange.DAYS_30 -> "30d"
                TimeRange.ALL -> "All"
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) AccentPurple else Color.Transparent)
                    .clickable { onRangeSelected(range) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = if (isSelected) TextWhite else TextGray,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    growth: Double
) {
    val isPositive = growth >= 0
    val growthColor = if (isPositive) SuccessGreen else ErrorRed
    val growthIcon = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

    Card(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = TextGray)

            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = TextWhite,
                fontWeight = FontWeight.Bold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = growthIcon,
                    contentDescription = null,
                    tint = growthColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${if(isPositive) "+" else ""}$growth%",
                    color = growthColor,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CapacityCard(stats: EventStats) {
    val progress = if (stats.capacityMax > 0) stats.capacityCurrent.toFloat() / stats.capacityMax.toFloat() else 0f
    val percentage = (progress * 100).toInt()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text("Event Capacity", style = MaterialTheme.typography.bodyMedium, color = TextGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${stats.capacityCurrent} / ${stats.capacityMax}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextWhite,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextWhite,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = AccentPurple,
                trackColor = Color(0xFF2C2C3E),
            )
        }
    }
}

// Helper
fun formatCurrency(amount: Double): String {
    return try {
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.format(amount)
    } catch (e: Exception) {
        "$${amount}"
    }
}