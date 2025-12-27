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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    onBackClick: () -> Unit,
    onPublishClick: () -> Unit
) {
    val viewModel = remember { AppModule.provideCreateEventViewModel() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- ESTADOS DO FORMULÁRIO (TRADUZIDOS) ---
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var venueName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var capacityText by remember { mutableStateOf("") }
    var latText by remember { mutableStateOf("38.7075") } // Lisboa por defeito
    var lonText by remember { mutableStateOf("-9.1455") }
    var isFeatured by remember { mutableStateOf(false) }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var expandedCategory by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(EventCategory.OTHER) }

    // Estado do Seletor de Mapa
    var showMapPicker by remember { mutableStateOf(false) }

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
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                withContext(Dispatchers.Main) { selectedImageBytes = bytes }
            }
        }
    }

    LaunchedEffect(Unit) {
        startDateDisplay = formatDisplay(startCalendar)
        endDateDisplay = formatDisplay(endCalendar)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Evento", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B0A12))
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    viewModel.createEvent(
                        title = title,
                        description = description,
                        locationName = venueName,
                        imageBytes = selectedImageBytes,
                        dateTime = formatToIso(startCalendar),
                        endDateTime = formatToIso(endCalendar),
                        category = selectedCategory.name,
                        price = priceText.toDoubleOrNull() ?: 0.0,
                        maxCapacity = capacityText.toIntOrNull() ?: 100,
                        latitude = latText.toDoubleOrNull() ?: 0.0,
                        longitude = lonText.toDoubleOrNull() ?: 0.0,
                        isFeatured = isFeatured,
                        onSuccess = { onPublishClick() },
                        onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() }
                    )
                },
                enabled = !isLoading && title.isNotBlank() && venueName.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7B61FF))
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Text("Publicar Evento", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // 1. Imagem de Capa
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF1E1E2C))
                        .clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Image, null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                            Text("Adicionar Capa", color = Color.Gray)
                        }
                    } else {
                        AsyncImage(model = selectedImageUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }
            }

            // 2. Info Básica
            item {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Título do Evento") }, modifier = Modifier.fillMaxWidth())
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
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = { selectedCategory = cat; expandedCategory = false }
                            )
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
                OutlinedTextField(value = venueName, onValueChange = { venueName = it }, label = { Text("Nome do Local (Ex: Arena Point)") }, modifier = Modifier.fillMaxWidth())

                Spacer(Modifier.height(12.dp))

                // BOTÃO PARA ABRIR O MAPA
                Button(
                    onClick = { showMapPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E2E3E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Outlined.Map, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Escolher no Mapa")
                }

                Spacer(Modifier.height(8.dp))

                // Mostrar coordenadas atuais (Apenas leitura para ser mais limpo)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = latText, onValueChange = { latText = it }, label = { Text("Lat") }, modifier = Modifier.weight(1f), readOnly = true)
                    OutlinedTextField(value = lonText, onValueChange = { lonText = it }, label = { Text("Lon") }, modifier = Modifier.weight(1f), readOnly = true)
                }
            }

            // 6. Configurações Finais
            item {
                SectionHeaderSmall("Configurações")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Preço (€)") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                    OutlinedTextField(value = capacityText, onValueChange = { capacityText = it }, label = { Text("Lotação") }, modifier = Modifier.weight(1f), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                }
                Spacer(Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Destacar evento na Home", color = Color.White)
                    Switch(checked = isFeatured, onCheckedChange = { isFeatured = it })
                }
            }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // --- DIALOG DO SELETOR DE MAPA ---
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

// --- COMPONENTE DO MAPA (DIALOG) ---


// --- FUNÇÕES UTILITÁRIAS ---

@Composable
fun SectionHeaderSmall(title: String) {
    Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(vertical = 8.dp))
}



fun formatToIso(calendar: Calendar): String = android.text.format.DateFormat.format("yyyy-MM-dd'T'HH:mm", calendar).toString()
fun formatDisplay(calendar: Calendar): String = android.text.format.DateFormat.format("dd MMM, HH:mm", calendar).toString()

private val BgDark = Color(0xFF0B0A12)
private val AccentPurple = Color(0xFF7B61FF)