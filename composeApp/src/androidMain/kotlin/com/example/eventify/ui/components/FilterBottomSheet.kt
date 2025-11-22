package com.example.eventify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
// IMPORTS CORRETOS DO MODELO KMM:
import com.example.eventify.model.EventCategory
import com.example.eventify.model.FilterState
import com.example.eventify.model.PriceType
import com.example.eventify.ui.theme.EventifyTheme
import kotlin.math.roundToInt

// --- Composable Principal do BottomSheet ---
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterBottomSheet(
    initialState: FilterState,
    onApply: (FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentState by remember { mutableStateOf(initialState) }

    val mockCategories = EventCategory.entries.toList()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // 1. Header
            Header(onReset = { currentState = FilterState() })

            // Coluna rolável para o conteúdo dos filtros
            Column(
                modifier = Modifier
                    .weight(1f, fill = false) // fill=false para não ocupar espaço vazio
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 2. Seção de Data
                DateRangeSection(
                    startDate = currentState.dateFrom ?: "",
                    endDate = currentState.dateTo ?: "",
                    onStartDateChange = { currentState = currentState.copy(dateFrom = it) },
                    onEndDateChange = { currentState = currentState.copy(dateTo = it) }
                )

                // 3. Seção de Localização
                LocationSection(
                    radius = currentState.locationRadiusKm.toFloat(),
                    useCurrentLocation = currentState.useCurrentLocation,
                    onRadiusChange = { currentState = currentState.copy(locationRadiusKm = it.roundToInt()) },
                    onToggleLocation = { currentState = currentState.copy(useCurrentLocation = it) }
                )

                // 4. Seção de Categoria
                CategorySection(
                    allCategories = mockCategories,
                    selectedCategories = currentState.categories,
                    onCategorySelect = { category ->
                        val updatedCategories = currentState.categories.toMutableSet()
                        if (updatedCategories.contains(category)) {
                            updatedCategories.remove(category)
                        } else {
                            updatedCategories.add(category)
                        }
                        currentState = currentState.copy(categories = updatedCategories)
                    }
                )

                // 5. Seção de Preço
                PriceSection(
                    selectedOption = currentState.priceType,
                    onOptionSelect = { currentState = currentState.copy(priceType = it) }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // 6. Botão de Ação Principal
            Button(
                onClick = {
                    onApply(currentState)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Apply Filters")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// --- Componentes de Suporte do Filtro ---

@Composable
private fun Header(onReset: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Filters", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        TextButton(onClick = onReset) {
            Text("Reset", color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun DateRangeSection(
    startDate: String,
    endDate: String,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Date Range")
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = startDate,
                onValueChange = onStartDateChange,
                label = { Text("Start Date") },
                trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = endDate,
                onValueChange = onEndDateChange,
                label = { Text("End Date") },
                trailingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
    }
}

@Composable
private fun LocationSection(
    radius: Float,
    useCurrentLocation: Boolean,
    onRadiusChange: (Float) -> Unit,
    onToggleLocation: (Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle("Location")
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Radius: ${radius.roundToInt()} km", style = MaterialTheme.typography.bodyLarge)
        }
        Slider(
            value = radius,
            onValueChange = onRadiusChange,
            valueRange = 1f..100f,
            steps = 99
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Use Current Location", style = MaterialTheme.typography.bodyLarge)
            Switch(checked = useCurrentLocation, onCheckedChange = onToggleLocation)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategorySection(
    allCategories: List<EventCategory>,
    selectedCategories: Set<EventCategory>,
    onCategorySelect: (EventCategory) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Category")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            allCategories.forEach { category ->
                val isSelected = selectedCategories.contains(category)

                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(category) },
                    label = {
                        Text(category.name.lowercase().replaceFirstChar { it.titlecase() })
                    },
                    leadingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, contentDescription = "Selected") }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Composable
private fun PriceSection(
    selectedOption: PriceType,
    onOptionSelect: (PriceType) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle("Price")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriceType.entries.forEach { option ->
                val isSelected = selectedOption == option
                FilterChip(
                    selected = isSelected,
                    onClick = { onOptionSelect(option) },
                    label = {
                        Text(option.name.lowercase().replaceFirstChar { it.titlecase() })
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FilterBottomSheetPreview() {
    EventifyTheme(darkTheme = true) {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.background).padding(16.dp)) {
            // Mock simples para preview
            Text("Filter Preview Placeholder")
        }
    }
}