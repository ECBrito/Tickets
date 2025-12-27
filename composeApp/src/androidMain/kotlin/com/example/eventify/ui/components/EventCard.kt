package com.example.eventify.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventify.model.Event
import com.example.eventify.ui.theme.EventifyTheme
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.InternalSerializationApi

// Helper para formatar data
private fun formatDateTime(dateTime: String): String {
    val parsed = LocalDateTime.parse(dateTime)

    val day = parsed.dayOfMonth.toString().padStart(2, '0')
    val month = parsed.month.name.take(3).lowercase().replaceFirstChar { it.uppercase() }
    val hour = parsed.hour.toString().padStart(2, '0')
    val minute = parsed.minute.toString().padStart(2, '0')

    return "$day $month, $hour:$minute"
}

@OptIn(InternalSerializationApi::class)
@Composable
fun EventCard(
    event: Event,
    onClick: (String) -> Unit,
    onSave: (String) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(event.id) },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Imagem
            AsyncImage(
                model = event.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.small)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Conteúdo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.category,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.locationName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                // Aqui usamos a propriedade dateTime que adicionámos ao modelo Event
                Text(
                    text = formatDateTime(event.dateTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botão Save
            IconButton(onClick = { onSave(event.id) }) {
                Icon(
                    imageVector = if (event.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (event.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}