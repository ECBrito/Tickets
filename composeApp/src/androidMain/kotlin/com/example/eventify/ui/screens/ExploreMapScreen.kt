package com.example.eventify.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.eventify.di.AppModule // <--- O segredo: usa o módulo de injeção
import com.example.eventify.model.Event
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreMapScreen(
    onBackToListView: () -> Unit,
    onEventClick: (String) -> Unit
) {
    // 1. Cria o ViewModel usando o AppModule (já configurado com o repositório certo)
    val viewModel = remember { AppModule.provideExploreViewModel() }

    // 2. Observa os eventos (a variável pública agora chama-se 'events')
    val events: List<Event> by viewModel.events.collectAsState()
    val isLoading: Boolean by viewModel.isLoading.collectAsState()

    // Posição inicial do mapa (Exemplo: Lisboa)
    val defaultLocation = LatLng(38.7223, -9.1393)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa de Eventos", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackToListView) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
            } else {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    events.forEach { event ->
                        // Converte a string "lat,lng" do Firebase para coordenadas do mapa
                        val coordinates = parseLocation(event.location)

                        if (coordinates != null) {
                            Marker(
                                state = MarkerState(position = coordinates),
                                title = event.title,
                                snippet = event.category,
                                onClick = {
                                    onEventClick(event.id)
                                    false // false = mantém o comportamento padrão (mostrar info window)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Função auxiliar para converter "38.7,-9.1" em objeto LatLng
private fun parseLocation(location: String): LatLng? {
    return try {
        val parts = location.split(",")
        if (parts.size >= 2) {
            val lat = parts[0].trim().toDouble()
            val lng = parts[1].trim().toDouble()
            LatLng(lat, lng)
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}