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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.EventCategory
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

    // --- ESTADOS ---
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }

    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(EventCategory.OTHER) }

    var currentImageUrl by remember { mutableStateOf("") }
    var newImageUri by remember { mutableStateOf<Uri?>(null) }
    var newImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    // --- DATAS (Start & End) ---
    val startCalendar = remember { Calendar.getInstance() }
    val endCalendar = remember { Calendar.getInstance() }

    var startDateDisplay by remember { mutableStateOf("") }
    var endDateDisplay by remember { mutableStateOf("") }

    // Helpers de Formatação
    fun formatToIso(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
        return "${year}-${month}-${day}T${hour}:${minute}"
    }

    fun formatDisplay(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
        return "$day $month, $hour:$minute"
    }

    // Helper para converter String ISO -> Calendar
    fun parseIsoToCalendar(isoString: String, targetCalendar: Calendar) {
        try {
            if (isoString.isBlank()) return
            // Formato esperado: YYYY-MM-DDTHH:mm
            val parts = isoString.split("T")
            val dateParts = parts[0].split("-")
            val timeParts = parts[1].split(":")

            targetCalendar.set(
                dateParts[0].toInt(),      // Year
                dateParts[1].toInt() - 1,  // Month (0-11)
                dateParts[2].toInt(),      // Day
                timeParts[0].toInt(),      // Hour
                timeParts[1].toInt()       // Minute
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // --- PRÉ-PREENCHIMENTO ---
    LaunchedEffect(eventToEdit) {
        eventToEdit?.let { event ->
            if (title.isEmpty()) title = event.title
            if (description.isEmpty()) description = event.description
            if (location.isEmpty()) location = event.location
            if (currentImageUrl.isEmpty()) currentImageUrl = event.imageUrl
            if (price.isEmpty()) price = event.price.toString()
            if (capacity.isEmpty()) capacity = event.maxCapacity.toString()

            try { selectedCategory = EventCategory.valueOf(event.category.uppercase()) } catch (e: Exception) {}

            // Preencher Datas
            if (startDateDisplay.isEmpty()) {
                parseIsoToCalendar(event.dateTime, startCalendar)
                startDateDisplay = formatDisplay(startCalendar)

                if (event.endDateTime.isNotBlank()) {
                    parseIsoToCalendar(event.endDateTime, endCalendar)
                    endDateDisplay = formatDisplay(endCalendar)
                } else {
                    // Se não tiver data de fim, assume Start + 2h
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
                        val priceVal = price.toDoubleOrNull() ?: 0.0
                        val capVal = capacity.toIntOrNull() ?: 100

                        viewModel.updateEvent(
                            title = title,
                            description = description,
                            location = location,
                            dateTime = formatToIso(startCalendar),    // Start Formatada
                            endDateTime = formatToIso(endCalendar),   // End Formatada
                            category = selectedCategory.name,
                            price = priceVal,
                            maxCapacity = capVal,
                            imageBytes = newImageBytes,
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
            // 1. IMAGEM
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

            // 2. CAMPOS BÁSICOS
            item {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
            }

            // 3. CATEGORIA
            item {
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory.name.lowercase().replaceFirstChar { it.titlecase() },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        EventCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name.lowercase().replaceFirstChar { it.titlecase() }) },
                                onClick = {
                                    selectedCategory = category
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }
            }

            // 4. DATAS (START E END)
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DateTimePickerField(
                        label = "Starts",
                        value = startDateDisplay,
                        modifier = Modifier.weight(1f),
                        context = context,
                        calendar = startCalendar,
                        onDateSelected = { startDateDisplay = formatDisplay(startCalendar) }
                    )
                    DateTimePickerField(
                        label = "Ends",
                        value = endDateDisplay,
                        modifier = Modifier.weight(1f),
                        context = context,
                        calendar = endCalendar,
                        onDateSelected = { endDateDisplay = formatDisplay(endCalendar) }
                    )
                }
            }

            // 5. PREÇO E LOTAÇÃO
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) price = it },
                        label = { Text("Price ($)") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { if (it.all { char -> char.isDigit() }) capacity = it },
                        label = { Text("Capacity") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }
    }
}

// (Mantém as funções uriToByteArray e DateTimePickerField no ficheiro ou importa-as)
// ...