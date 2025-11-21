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
import com.example.eventify.repository.EventRepositoryImplKMM
import com.example.eventify.viewmodels.ExploreMapViewModelKMM
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreMapScreen(
    onBackToListView: () -> Unit
) {
    val viewModel = remember { ExploreMapViewModelKMM(repository = EventRepositoryImplKMM()) }
    val events by viewModel.events.collectAsState()

    val lisbon = LatLng(38.7223, -9.1393)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(lisbon, 12f)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Event Map", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackToListView) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
                cameraPositionState = cameraPositionState
            ) {
                events.forEach { event ->
                    val coordinates = parseLocation(event.location)
                    if (coordinates != null) {
                        Marker(
                            state = MarkerState(position = coordinates),
                            title = event.title,
                            snippet = event.category.name
                        )
                    }
                }
            }
        }
    }
}

// Helper para converter string "lat,lng" em LatLng
private fun parseLocation(location: String): LatLng? {
    return try {
        val parts = location.split(",")
        LatLng(parts[0].trim().toDouble(), parts[1].trim().toDouble())
    } catch (e: Exception) {
        null
    }
}
