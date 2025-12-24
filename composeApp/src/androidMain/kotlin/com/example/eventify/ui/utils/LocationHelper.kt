package com.example.eventify.ui.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices

@SuppressLint("MissingPermission") // Só chamamos se tiver permissão
fun getCurrentLocation(context: Context, onLocationFetched: (Double, Double) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            // Recebemos a localização (pode ser null se o GPS estiver desligado há muito tempo)
            if (location != null) {
                onLocationFetched(location.latitude, location.longitude)
            }
        }
        .addOnFailureListener {
            // Falhou (GPS desligado ou erro), ignoramos por agora
            it.printStackTrace()
        }
}