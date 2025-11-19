package com.example.eventify.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.eventify.model.EventCategory
import kotlinx.datetime.LocalDateTime

private fun formatDateTime(dateTime: LocalDateTime): String {
    val dayOfWeek = dateTime.dayOfWeek.name.take(3)
    val month = dateTime.month.name.take(3)
    val day = dateTime.dayOfMonth
    val hour = dateTime.hour.toString().padStart(2, '0')
    val minute = dateTime.minute.toString().padStart(2, '0')

    return "$dayOfWeek, $month $day, $hour:$minute"
}

private fun formatCategory(category: EventCategory): String {
    return category.name.lowercase().replaceFirstChar { it.titlecase() }
}

@Composable
fun EventCard(
    event: Event,
    onClick: (String) -> Unit,
    onSave: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(event.id) },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = "Poster for ${event.title}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .aspectRatio(1f)
                    .clip(MaterialTheme.shapes.small)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatCategory(event.category),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    IconButton(
                        onClick = { onSave(event.id) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = if (event.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (event.isSaved) "Unsave Event" else "Save Event",
                            tint = if (event.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.location,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatDateTime(event.dateTime),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0B0A12)
@Composable
fun EventCardPreview() {
    val mockEvent = Event(
        id = "1",
        title = "Live Jazz Night",
        category = EventCategory.CONCERT,
        location = "The Blue Note Club",
        dateTime = LocalDateTime(2024, 8, 9, 20, 0),
        imageUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=870&q=80",
        price = 50.0,
        currency = "USD",
        isSaved = true,
        isRegistered = false,
        organizer = "Jazz Fest Inc."
    )

    com.example.eventify.ui.theme.EventifyTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            EventCard(
                event = mockEvent,
                onClick = {},
                onSave = {}
            )

            EventCard(
                event = mockEvent.copy(
                    id = "2",
                    title = "Modern Art Exhibit",
                    category = EventCategory.WORKSHOP,
                    location = "Metropolitan Art Gall...",
                    isSaved = false,
                    dateTime = LocalDateTime(2024, 8, 10, 10, 0)
                ),
                onClick = {},
                onSave = {}
            )
        }
    }
}