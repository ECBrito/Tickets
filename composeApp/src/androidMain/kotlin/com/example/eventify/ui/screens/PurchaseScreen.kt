package com.example.eventify.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventify.di.AppModule
import com.example.eventify.ui.Screen
import kotlinx.serialization.InternalSerializationApi

@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
@Composable
fun PurchaseScreen(
    eventId: String,
    navController: NavController
) {
    val viewModel = remember { AppModule.providePurchaseViewModel(eventId) }
    val event by viewModel.event.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var quantity by remember { mutableIntStateOf(1) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buy Tickets") },
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.confirmPurchase(quantity) {
                        // Sucesso! Vai para My Events e limpa o stack
                        navController.navigate(Screen.HOME_ROOT) {
                            popUpTo(Screen.HOME_ROOT) { inclusive = true }
                        }
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else Text("Confirm Payment - Total: $${(event?.price ?: 0.0) * quantity}")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            event?.let { evt ->
                Text(evt.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Price per ticket: $${evt.price}", style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.height(32.dp))

                Text("Quantity", style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledIconButton(onClick = { if (quantity > 1) quantity-- }) { Icon(Icons.Default.Remove, null) }

                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )

                    FilledIconButton(onClick = { quantity++ }) { Icon(Icons.Default.Add, null) }
                }
            }
        }
    }
}