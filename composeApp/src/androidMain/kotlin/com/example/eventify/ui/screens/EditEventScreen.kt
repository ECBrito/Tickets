package com.example.eventify.ui.screens.organizer

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.EventCategory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, InternalSerializationApi::class)
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

    // --- ESTADOS DO FORMULÁRIO (TRADUZIDOS) ---
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var venueName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var capacityText by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf("") }
    var lonText by remember { mutableStateOf("") }
    var isFeatured by remember { mutableStateOf(false) }

    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(EventCategory.OTHER) }

    var currentImageUrl by remember { mutableStateOf("") }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    var newImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Estado do Mapa
    var showMapPicker by remember { mutableStateOf(false) }

    // Datas
    val startCalendar = remember { Calendar.getInstance() }
    val endCalendar = remember { Calendar.getInstance() }
    var startDateDisplay by remember { mutableStateOf("") }
    var endDateDisplay by remember { mutableStateOf("") }

    // --- CARREGAR DADOS EXISTENTES ---
    LaunchedEffect(eventToEdit) {
        eventToEdit?.let { event ->
            if (title.isEmpty()) title = event.title
            if (description.isEmpty()) description = event.description
            if (venueName.isEmpty()) venueName = event.locationName
            if (currentImageUrl.isEmpty()) currentImageUrl = event.imageUrl
            if (priceText.isEmpty()) priceText = event.price.toString()
            if (capacityText.isEmpty()) capacityText = event.maxCapacity.toString()
            if (latText.isEmpty()) latText = event.latitude.toString()
            if (lonText.isEmpty()) lonText = event.longitude.toString()
            isFeatured = event.isFeatured

            try { selectedCategory = EventCategory.valueOf(event.category.uppercase()) } catch (e: Exception) {}

            if (startDateDisplay.isEmpty()) {
                parseIsoToCalendar(event.dateTime, startCalendar)
                startDateDisplay = formatDisplay(startCalendar)

                if (event.endDateTime.isNotBlank()) {
                    parseIsoToCalendar(event.endDateTime, endCalendar)
                    endDateDisplay = formatDisplay(endCalendar)
                } else {
                    endCalendar.timeInMillis = startCalendar.timeInMillis + (2 * 60 * 60 * 1000)
                    endDateDisplay = formatDisplay(endCalendar)
                }
            }
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
                title = { Text("Editar Evento", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0A12))
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    scope.launch {
                        viewModel.updateEvent(
                            title = title,
                            description = description,
                            locationName = venueName,
                            dateTime = formatToIso(startCalendar),
                            endDateTime = formatToIso(endCalendar),
                            category = selectedCategory.name,
                            price = priceText.toDoubleOrNull() ?: 0.0,
                            maxCapacity = capacityText.toIntOrNull() ?: 100,
                            latitude = latText.toDoubleOrNull() ?: 0.0,
                            longitude = lonText.toDoubleOrNull() ?: 0.0,
                            isFeatured = isFeatured,
                            imageBytes = newImageBytes,
                            onSuccess = onSaveClick,
                            onError = { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Guardar Alterações", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // 1. Imagem
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1E2C))
                        .clickable { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    val displayImage = newImageUri ?: if (currentImageUrl.isNotEmpty()) currentImageUrl else null
                    if (displayImage != null) {
                        AsyncImage(model = displayImage, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Outlined.Image, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                    }
                }
            }

            // 2. Info Básica
            item {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descrição") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
            }

            // 3. Categoria
            item {
                ExposedDropdownMenuBox(expanded = expandedCategory, onExpandedChange = { expandedCategory = !expandedCategory }) {
                    OutlinedTextField(
                        value = selectedCategory.name.lowercase().replaceFirstChar { it.titlecase() },
                        onValueChange = {}, readOnly = true, label = { Text("Categoria") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedCategory, onDismissRequest = { expandedCategory = false }) {
                        EventCategory.entries.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategory = cat; expandedCategory = false })
                        }
                    }
                }
            }

            // 4. Datas
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateTimePickerField("Início", startDateDisplay, Modifier.weight(1f), context, startCalendar) { startDateDisplay = formatDisplay(startCalendar) }
                    DateTimePickerField("Fim", endDateDisplay, Modifier.weight(1f), context, endCalendar) { endDateDisplay = formatDisplay(endCalendar) }
                }
            }

            // 5. Localização & MAPA
            item {
                SectionHeaderSmall("Localização")
                OutlinedTextField(value = venueName, onValueChange = { venueName = it }, label = { Text("Nome do Local") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = { showMapPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E3E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Map, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Escolher Local no Mapa")
                }

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = latText, onValueChange = { latText = it }, label = { Text("Lat") }, modifier = Modifier.weight(1f), readOnly = true)
                    OutlinedTextField(value = lonText, onValueChange = { lonText = it }, label = { Text("Lon") }, modifier = Modifier.weight(1f), readOnly = true)
                }
            }

            // 6. Configurações
            item {
                SectionHeaderSmall("Configurações")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Preço (€)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = capacityText, onValueChange = { capacityText = it }, label = { Text("Lotação") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Destacar evento na Home", color = Color.White)
                    Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // --- DIALOG DO MAPA ---
    if (showMapPicker) {
        MapPickerFullDialog(
            initialLat = latText.toDoubleOrNull() ?: 38.7075,
            initialLon = lonText.toDoubleOrNull() ?: -9.1455,
            onLocationSelected = { lat, lon ->
                latText = String.format("%.6f", lat)
                lonText = String.format("%.6f", lon)
                showMapPicker = false
            },
            onDismiss = { showMapPicker = false }
        )
    }
}

// --- FUNÇÕES UTILITÁRIAS (UMA ÚNICA VEZ NO FIM DO FICHEIRO) ---

@Composable
fun MapPickerFullDialog(
    initialLat: Double,
    initialLon: Double,
    onLocationSelected: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var markerPosition by remember { mutableStateOf(LatLng(initialLat, initialLon)) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(markerPosition, 15f)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF0B0A12)) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, null, tint = Color.White) }
                    Text("Mova o mapa para o local", color = Color.White, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { onLocationSelected(markerPosition.latitude, markerPosition.longitude) }) {
                        Text("Confirmar", color = Color(0xFF7B61FF), fontWeight = FontWeight.Bold)
                    }
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { markerPosition = it }
                ) {
                    Marker(state = MarkerState(position = markerPosition))
                }
            }
        }
    }
}



@Composable
fun DateTimePickerField(label: String, value: String, modifier: Modifier, context: Context, calendar: Calendar, onDateSelected: () -> Unit) {
    val tpd = TimePickerDialog(context, { _, h, m -> calendar.set(Calendar.HOUR_OF_DAY, h); calendar.set(Calendar.MINUTE, m); onDateSelected() }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true)
    val dpd = DatePickerDialog(context, { _, y, m, d -> calendar.set(y, m, d); tpd.show() }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

    OutlinedTextField(
        value = value, onValueChange = {}, readOnly = true, label = { Text(label) },
        trailingIcon = { Icon(Icons.Default.CalendarMonth, null) },
        modifier = modifier.clickable { dpd.show() },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(disabledTextColor = Color.White, disabledBorderColor = Color.Gray)
    )
}



fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
    return try { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } } catch (e: Exception) { null }
}

fun parseIsoToCalendar(isoString: String, targetCalendar: Calendar) {
    try {
        if (isoString.isBlank()) return
        val parts = isoString.split("T")
        val dateParts = parts[0].split("-")
        val timeParts = parts[1].split(":")
        targetCalendar.set(dateParts[0].toInt(), dateParts[1].toInt() - 1, dateParts[2].toInt(), timeParts[0].toInt(), timeParts[1].toInt())
    } catch (e: Exception) { }
}