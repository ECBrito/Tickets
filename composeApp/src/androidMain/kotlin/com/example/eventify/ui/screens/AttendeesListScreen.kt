package com.example.eventify.ui.screens.organizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download // <--- Ícone Novo
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.Attendee
import com.example.eventify.ui.utils.exportAttendeesToPdf // <--- Importa a função
import kotlinx.serialization.InternalSerializationApi

@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun AttendeesListScreen(
    eventId: String,
    navController: NavController
) {
    val viewModel = remember { AppModule.provideAttendeesViewModel(eventId) }
    val attendees by viewModel.attendees.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current // <--- Contexto para o PDF

    Scaffold(
        containerColor = Color(0xFF0B0A12),
        topBar = {
            TopAppBar(
                title = { Text("Attendees List", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                },
                actions = {
                    // BOTÃO EXPORTAR PDF
                    IconButton(onClick = {
                        if (attendees.isNotEmpty()) {
                            exportAttendeesToPdf(context, "My Event", attendees)
                        }
                    }) {
                        Icon(Icons.Default.Download, "Export PDF", tint = Color(0xFF7B61FF))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0A12))
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {

            // Barra de Pesquisa
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or email", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color(0xFF7B61FF),
                    focusedBorderColor = Color(0xFF7B61FF),
                    unfocusedBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF7B61FF))
                }
            } else {
                Text(
                    text = "${attendees.size} Guests",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                if (attendees.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No tickets sold yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(attendees) { attendee ->
                            AttendeeItem(
                                attendee = attendee,
                                onCheckInClick = { viewModel.manualCheckIn(attendee.ticketId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(InternalSerializationApi::class)
@Composable
fun AttendeeItem(attendee: Attendee, onCheckInClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151520)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            if (attendee.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = attendee.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(50.dp).clip(CircleShape)
                )
            } else {
                Surface(shape = CircleShape, color = Color(0xFF2C2C3E), modifier = Modifier.size(50.dp)) {
                    Icon(Icons.Default.Person, null, tint = Color.Gray, modifier = Modifier.padding(12.dp))
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(attendee.name, style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text(attendee.email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            // Botão Check-in
            if (attendee.isCheckedIn) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Checked In", tint = Color(0xFF00E096), modifier = Modifier.size(32.dp))
            } else {
                Button(
                    onClick = onCheckInClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Check-in")
                }
            }
        }
    }
}