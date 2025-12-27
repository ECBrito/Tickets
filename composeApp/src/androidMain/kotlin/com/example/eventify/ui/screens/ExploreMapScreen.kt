package com.example.eventify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.eventify.di.AppModule
import com.example.eventify.model.Event
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.serialization.InternalSerializationApi

@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun ExploreMapScreen(
    onBackToListView: () -> Unit,
    onEventClick: (String) -> Unit
) {
    val viewModel = remember { AppModule.provideExploreViewModel() }
    val events by viewModel.events.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // Posição inicial: Lisboa por defeito ou o primeiro evento
    val initialPos = if (events.isNotEmpty()) LatLng(events.first().latitude, events.first().longitude)
    else LatLng(38.7223, -9.1393)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialPos, 11f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Eventos", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackToListView) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0A12))
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color(0xFFD0BCFF))
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = true),
                    uiSettings = MapUiSettings(myLocationButtonEnabled = true)
                ) {
                    events.forEach { event ->
                        // Verifica se o evento tem coordenadas válidas antes de criar o Marker
                        if (event.latitude != 0.0 && event.longitude != 0.0) {
                            Marker(
                                state = MarkerState(position = LatLng(event.latitude, event.longitude)),
                                title = event.title,
                                snippet = "${event.locationName} • ${if (event.price == 0.0) "Grátis" else "${event.price}€"}",
                                onInfoWindowClick = {
                                    onEventClick(event.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}