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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.EventCategory
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit
) {
    val viewModel = remember { AppModule.provideCreateEventViewModel() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- ESTADOS DO FORMULÁRIO ---
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Categoria (Dropdown)
    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(EventCategory.OTHER) }

    // Datas (Start & End)
    // Guardamos objetos Calendar para manipulação e Strings para display
    val startCalendar = remember { Calendar.getInstance() }
    val endCalendar = remember { Calendar.getInstance().apply { add(Calendar.HOUR, 2) } }

    var startDateDisplay by remember { mutableStateOf("") }
    var endDateDisplay by remember { mutableStateOf("") }

    val isLoading by viewModel.loading.collectAsState()

    // Launcher da Galeria
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    // Função auxiliar para formatar data para o formato ISO-8601 (Necessário para o EventCard não crashar)
    fun formatToIso(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        val month = (calendar.get(Calendar.MONTH) + 1).toString().padStart(2, '0')
        val day = calendar.get(Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
        val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
        return "${year}-${month}-${day}T${hour}:${minute}"
    }

    // Função auxiliar para mostrar data bonita na UI
    fun formatDisplay(calendar: Calendar): String {
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
        val hour = calendar.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
        val minute = calendar.get(Calendar.MINUTE).toString().padStart(2, '0')
        return "$day $month, $hour:$minute"
    }

    // Inicializar displays
    LaunchedEffect(Unit) {
        startDateDisplay = formatDisplay(startCalendar)
        endDateDisplay = formatDisplay(endCalendar)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Event", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF151520))
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    scope.launch {
                        viewModel.createEvent(
                            title = title,
                            description = description,
                            location = location,
                            imageUrl = selectedImageUri?.toString(),
                            // Enviamos a Data de INÍCIO formatada corretamente
                            dateTime = formatToIso(startCalendar),
                            category = selectedCategory.name,
                            onSuccess = { onPublishClick() },
                            onError = { msg ->
                                Toast.makeText(context, "Error: $msg", Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                },
                enabled = !isLoading && title.isNotBlank() && location.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Publish Event")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. SELEÇÃO DE IMAGEM
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1F1F2E))
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Add Cover Image", color = Color.Gray)
                        }
                    } else {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // 2. TÍTULO E DESCRIÇÃO
            item {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Event Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }

            // 3. CATEGORIA (Dropdown)
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

            // 4. DATAS (Start & End Pickers)
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // START DATE
                    DateTimePickerField(
                        label = "Starts",
                        value = startDateDisplay,
                        modifier = Modifier.weight(1f),
                        context = context,
                        calendar = startCalendar,
                        onDateSelected = { startDateDisplay = formatDisplay(startCalendar) }
                    )

                    // END DATE
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

            // 5. LOCALIZAÇÃO (Mantido como texto por enquanto)
            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") }, // "We will change too but not yet"
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

// --- Helper Component para Date+Time Picker ---
@Composable
fun DateTimePickerField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    context: Context,
    calendar: Calendar,
    onDateSelected: () -> Unit
) {
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            onDateSelected()
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true // 24h format
    )

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            // Depois de escolher a data, abre o relógio automaticamente
            timePickerDialog.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
        modifier = modifier.clickable { datePickerDialog.show() },
        enabled = false, // Desabilita input manual, mas o clickable acima funciona no Box pai se usarmos Box, mas aqui usamos enabled=false e clickable no modifier
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}