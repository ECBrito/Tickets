package com.example.eventify.ui.screens.organizer

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.eventify.model.EventCategory
import com.example.eventify.ui.components.EventLocationPicker // O nosso componente de Mapa
import com.example.eventify.ui.theme.EventifyTheme
import com.example.eventify.ui.viewmodels.CreateEventViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit,
    viewModel: CreateEventViewModel = viewModel() // Injeta o ViewModel
) {
    // --- Estados do Formulário ---
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Categoria
    var category by remember { mutableStateOf("") }
    var isCategoryExpanded by remember { mutableStateOf(false) }

    // Datas
    var startDate by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }

    var endDate by remember { mutableStateOf("") }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Localização
    var location by remember { mutableStateOf("") }

    // Validação e Imagem
    var isTitleError by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Lançador para escolher foto da galeria
    val singlePhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> selectedImageUri = uri }
    )

    // Estados dos Date Pickers (Material 3)
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()

    // --- Diálogos de Data ---

    // Picker Data de Início
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    startDatePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        startDate = "${date.dayOfWeek.name.take(3)}, ${date.dayOfMonth} ${date.month.name.take(3)} ${date.year} • 19:00"
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = startDatePickerState) }
    }

    // Picker Data de Fim
    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    endDatePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.currentSystemDefault())
                        endDate = "${date.dayOfWeek.name.take(3)}, ${date.dayOfMonth} ${date.month.name.take(3)} ${date.year} • 22:00"
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = endDatePickerState) }
    }

    // --- UI Principal ---
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Create Event",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            CreateEventBottomBar(
                onSaveDraft = { /* Lógica Draft futura */ },
                onPublish = {
                    if (title.isEmpty()) {
                        isTitleError = true
                    } else {
                        // Chama o ViewModel para criar o evento
                        viewModel.createEvent(
                            title = title,
                            description = description,
                            categoryName = category,
                            location = location,
                            dateStr = startDate,
                            imageUri = selectedImageUri?.toString(),
                            onSuccess = {
                                onPublishClick() // Navega de volta
                            }
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // 1. Upload Imagem
            item {
                ImageUploadSection(
                    selectedImageUri = selectedImageUri,
                    onUploadClick = {
                        singlePhotoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }

            // 2. Título
            item {
                FormInputSection(
                    label = "Event Title",
                    value = title,
                    onValueChange = {
                        title = it
                        isTitleError = false
                    },
                    placeholder = "Enter a catchy title",
                    isError = isTitleError,
                    errorMessage = "Title is required."
                )
            }

            // 3. Descrição
            item {
                FormInputSection(
                    label = "Description",
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "Tell attendees about your event",
                    singleLine = false,
                    minLines = 4
                )
            }

            // 4. Categoria
            item {
                Box {
                    ClickableInputSection(
                        label = "Category",
                        value = category,
                        placeholder = "Select a category",
                        onClick = { isCategoryExpanded = true },
                        trailingIcon = null
                    )

                    DropdownMenu(
                        expanded = isCategoryExpanded,
                        onDismissRequest = { isCategoryExpanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        EventCategory.entries.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    category = cat.name
                                    isCategoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // 5. Datas
            item {
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    ClickableInputSection(
                        label = "Start Date & Time",
                        value = startDate,
                        placeholder = "Select date",
                        onClick = { showStartDatePicker = true },
                        leadingIcon = Icons.Default.CalendarMonth
                    )
                    ClickableInputSection(
                        label = "End Date & Time",
                        value = endDate,
                        placeholder = "Select date",
                        onClick = { showEndDatePicker = true },
                        leadingIcon = Icons.Default.CalendarMonth
                    )
                }
            }

            // 6. Localização e Mapa
            item {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    FormInputSection(
                        label = "Venue / Address",
                        value = location,
                        onValueChange = { location = it },
                        placeholder = "Tap on map below to select",
                        leadingIcon = Icons.Default.LocationOn
                    )

                    Text("Select Location on Map", color = Color.White, style = MaterialTheme.typography.titleMedium)

                    // MAPA REAL
                    EventLocationPicker(
                        onLocationSelected = { latLng ->
                            location = "${latLng.latitude}, ${latLng.longitude}"
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

// --- Componentes Auxiliares ---

@Composable
fun ImageUploadSection(
    selectedImageUri: Uri?,
    onUploadClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF151520))
            .clickable { onUploadClick() },
        contentAlignment = Alignment.Center
    ) {
        if (selectedImageUri != null) {
            AsyncImage(
                model = selectedImageUri,
                contentDescription = "Selected Cover Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
            Icon(Icons.Default.Edit, contentDescription = "Change", tint = Color.White, modifier = Modifier.size(48.dp))
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Image,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Upload cover image",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Tap to select an image from your gallery",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onUploadClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    contentPadding = PaddingValues(horizontal = 24.dp)
                ) {
                    Text("Upload Image", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun FormInputSection(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    leadingIcon: ImageVector? = null,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = singleLine,
            minLines = minLines,
            isError = isError,
            leadingIcon = if (leadingIcon != null) {
                { Icon(leadingIcon, contentDescription = null, tint = Color.Gray) }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = Color(0xFF151520),
                unfocusedContainerColor = Color(0xFF151520),
                errorBorderColor = MaterialTheme.colorScheme.error,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun ClickableInputSection(
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF151520),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(leadingIcon, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(12.dp))
                }

                Text(
                    text = if (value.isNotEmpty()) value else placeholder,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (value.isNotEmpty()) Color.White else Color.Gray,
                    modifier = Modifier.weight(1f)
                )

                if (trailingIcon != null) {
                    Icon(trailingIcon, contentDescription = null, tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun CreateEventBottomBar(
    onSaveDraft: () -> Unit,
    onPublish: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onSaveDraft,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Text("Draft", color = Color.White)
            }

            Button(
                onClick = onPublish,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Publish", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Preview
@Composable
fun CreateEventScreenPreview() {
    EventifyTheme(darkTheme = true) {
        CreateEventScreen(onBackClick = {}, onPublishClick = {})
    }
}