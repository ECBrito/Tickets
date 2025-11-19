package com.example.eventify.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

// Componente para SELECIONAR localização (usado no CreateEvent)
@Composable
fun EventLocationPicker(
    modifier: Modifier = Modifier,
    initialLocation: LatLng = LatLng(38.7223, -9.1393), // Lisboa
    onLocationSelected: (LatLng) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLocation, 12f)
    }

    var selectedLocation by remember { mutableStateOf(initialLocation) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            onMapClick = { latLng ->
                selectedLocation = latLng
                onLocationSelected(latLng)
            },
            uiSettings = MapUiSettings(zoomControlsEnabled = true)
        ) {
            Marker(
                state = MarkerState(position = selectedLocation),
                title = "Localização do Evento",
                snippet = "Ponto Selecionado"
            )
        }
    }
}

// Componente para VISUALIZAR localização (usado no Detail)
@Composable
fun EventLocationViewer(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    val eventLocation = LatLng(latitude, longitude)

    // O CameraPositionState é lembrado, mas precisamos de o atualizar se as coordenadas mudarem
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(eventLocation, 15f)
    }

    // Efeito para mover a câmara se a localização mudar (ex: ao navegar entre eventos)
    LaunchedEffect(eventLocation) {
        cameraPositionState.animate(
            update = CameraUpdateFactory.newLatLngZoom(eventLocation, 15f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                scrollGesturesEnabled = false, // Mapa estático (apenas visualização)
                zoomGesturesEnabled = true,    // Permite zoom com dois dedos
                rotationGesturesEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = true // Permite abrir no Google Maps App
            )
        ) {
            Marker(
                state = MarkerState(position = eventLocation),
                title = "Local do Evento"
            )
        }
    }
}