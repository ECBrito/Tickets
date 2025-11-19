package com.example.eventify.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventify.model.Event
import com.example.eventify.ui.viewmodels.ExploreViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreMapScreen(
    onBackToListView: () -> Unit,
    onEventClick: (String) -> Unit,
    viewModel: ExploreViewModel = viewModel()
) {
    val filteredEvents by viewModel.filteredEvents.collectAsState()

    // Posição inicial (Lisboa como exemplo, ou centro dos eventos)
    val lisbon = LatLng(38.7223, -9.1393)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(lisbon, 12f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Map", style = MaterialTheme.typography.titleLarge, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackToListView) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to List", tint = Color.White)
                    }
                },
                actions = {
                    // Botão para alternar de volta para Lista (redundante com o Back, mas bom para UX)
                    IconButton(onClick = onBackToListView) {
                        Icon(Icons.Default.List, contentDescription = "List View", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(zoomControlsEnabled = false)
            ) {
                // Desenha um marcador para cada evento
                filteredEvents.forEach { event ->
                    // Tenta converter a localização em LatLng
                    val coordinates = parseLocation(event.location)
                    if (coordinates != null) {
                        Marker(
                            state = MarkerState(position = coordinates),
                            title = event.title,
                            snippet = event.category.name,
                            onClick = {
                                onEventClick(event.id)
                                false // Retorna false para permitir o comportamento padrão (abrir info window)
                            }
                        )
                    }
                }
            }
        }
    }
}

// Helper simples para extrair coordenadas da string "lat, lng"
private fun parseLocation(location: String): LatLng? {
    return try {
        val parts = location.split(",")
        if (parts.size == 2) {
            LatLng(parts[0].trim().toDouble(), parts[1].trim().toDouble())
        } else null
    } catch (e: Exception) {
        null
    }
}