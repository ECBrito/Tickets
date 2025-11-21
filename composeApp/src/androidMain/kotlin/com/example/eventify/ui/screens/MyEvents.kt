package com.example.eventify.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import com.example.eventify.ui.viewmodels.MyEventsKMM

@Composable
fun MyEventsScreen(
    navController: NavController,
    viewModel: MyEventsKMM = MyEventsKMM()
) {
    val myEvents by viewModel.myEvents.collectAsState(initial = emptyList())

    LazyColumn {
        items(myEvents) { event ->
            EventCard(
                event = event,
                onClick = { navController.navigate(Screen.eventDetail(event.id)) },
                onSave = { viewModel.toggleSave(event.id) }
            )
        }
    }
}
