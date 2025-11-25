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
import androidx.compose.foundation.text.KeyboardOptions // <--- Import Novo
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
import androidx.compose.ui.text.input.KeyboardType // <--- Import Novo
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
fun CreateEventScreen(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit
) {
    val viewModel = remember { AppModule.provideCreateEventViewModel() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados do Formulário
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // NOVOS CAMPOS (Strings para facilitar input, convertemos ao enviar)
    var price by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }

    // IMAGEM
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(EventCategory.OTHER) }

    // Datas
    val startCalendar = remember { Calendar.getInstance() }
    val endCalendar = remember { Calendar.getInstance().apply { add(Calendar.HOUR, 2) } }
    var startDateDisplay by remember { mutableStateOf("") }
    var endDateDisplay by remember { mutableStateOf("") }

    val isLoading by viewModel.loading.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            scope.launch(Dispatchers.IO) {
                val bytes = uriToByteArray(context, uri)
                withContext(Dispatchers.Main) {
                    selectedImageBytes = bytes
                }
            }
        }
    }

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
                        val priceVal = price.toDoubleOrNull() ?: 0.0
                        val capVal = capacity.toIntOrNull() ?: 100

                        // Converter imagem
                        val imageBytes = if (selectedImageUri != null) {
                            withContext(Dispatchers.IO) {
                                uriToByteArray(context, selectedImageUri!!)
                            }
                        } else null

                        viewModel.createEvent(
                            title = title,
                            description = description,
                            location = location,
                            imageUrl = null,
                            imageBytes = imageBytes,

                            // --- DATAS ---
                            dateTime = formatToIso(startCalendar),   // Início
                            endDateTime = formatToIso(endCalendar),  // <--- Fim (FALTAVA ISTO!)

                            category = selectedCategory.name,
                            price = priceVal,
                            maxCapacity = capVal,
                            onSuccess = { onPublishClick() },
                            onError = { msg -> Toast.makeText(context, "Error: $msg", Toast.LENGTH_LONG).show() }
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
            // 1. Imagem
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

            // 2. Campos de Texto Básicos
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

            // 3. Categoria
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

            // 4. Datas
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

            // 5. Localização
            item {
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // 6. Preço e Lotação (NOVOS CAMPOS)
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = price,
                        onValueChange = { if (it.all { char -> char.isDigit() || char == '.' }) price = it },
                        label = { Text("Price ($)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    OutlinedTextField(
                        value = capacity,
                        onValueChange = { if (it.all { char -> char.isDigit() }) capacity = it },
                        label = { Text("Max Capacity") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }
    }
}

// --- FUNÇÃO MÁGICA: Converte Uri do Android para ByteArray ---
fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream?.use { it.readBytes() }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

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
        true
    )

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
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
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    )
}