package com.example.eventify.ui.screens.organizer

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.EventCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEventScreen(
    eventId: String,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    val viewModel = remember { AppModule.provideEditEventViewModel(eventId) }
    val eventToEdit by viewModel.event.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var categoryName by remember { mutableStateOf("") } // String simples para simplificar

    // Imagem
    var currentImageUrl by remember { mutableStateOf("") }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    var newImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Data (String ISO direta para simplificar a edição)
    var dateTimeIso by remember { mutableStateOf("") }

    // --- PREENCHER DADOS QUANDO CARREGA ---
    LaunchedEffect(eventToEdit) {
        eventToEdit?.let { event ->
            if (title.isEmpty()) title = event.title
            if (description.isEmpty()) description = event.description
            if (location.isEmpty()) location = event.location
            if (categoryName.isEmpty()) categoryName = event.category
            if (currentImageUrl.isEmpty()) currentImageUrl = event.imageUrl
            if (dateTimeIso.isEmpty()) dateTimeIso = event.dateTime
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            newImageUri = uri
            scope.launch(Dispatchers.IO) {
                newImageBytes = uriToByteArray(context, uri)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Event", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF151520))
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    scope.launch {
                        viewModel.updateEvent(
                            title = title,
                            description = description,
                            location = location,
                            dateTime = dateTimeIso, // Usa a data original ou editada
                            category = categoryName,
                            imageBytes = newImageBytes, // Envia bytes apenas se mudou a foto
                            onSuccess = onSaveClick,
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Save Changes")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Imagem (Mostra a Nova se existir, senão a Atual)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1F1F2E))
                        .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (newImageUri != null) {
                        AsyncImage(model = newImageUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else if (currentImageUrl.isNotEmpty()) {
                        AsyncImage(model = currentImageUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Outlined.Image, null, tint = Color.Gray)
                    }
                }
            }

            item { OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3) }
            item { OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth()) }

            // Data (Simplificada como texto para edição rápida, idealmente seria o Picker de novo)
            item { OutlinedTextField(value = dateTimeIso, onValueChange = { dateTimeIso = it }, label = { Text("Date (ISO)") }, modifier = Modifier.fillMaxWidth()) }
        }
    }
}