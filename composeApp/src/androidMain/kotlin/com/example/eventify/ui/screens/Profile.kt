package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.UserProfile
import com.example.eventify.ui.theme.EventifyTheme
import kotlinx.serialization.InternalSerializationApi

@OptIn(InternalSerializationApi::class)
@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    onOrganizerClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onInterestsClick: () -> Unit // <--- NOVO PARÂMETRO
) {
    val viewModel = remember { AppModule.provideEditProfileViewModel() }
    val profile by viewModel.profile.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(profile)

        // --- Botão para mudar para Organizador ---
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
            Button(
                onClick = onOrganizerClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Switch to Organizer Mode", style = MaterialTheme.typography.titleMedium)
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 24.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        // Definições da Conta
        SettingsSection(title = "Account") {
            SettingsItem(
                icon = Icons.Default.Person,
                title = "Personal Information",
                onClick = onEditProfileClick
            )
            SettingsItem(icon = Icons.Default.Payment, title = "Payment Methods", onClick = {})
            SettingsItem(icon = Icons.Default.Security, title = "Security", onClick = {})
        }

        // Preferências
        SettingsSection(title = "Preferences") {
            // --- BOTÃO DE INTERESSES ---
            SettingsItem(
                icon = Icons.Default.Favorite,
                title = "My Interests",
                onClick = onInterestsClick // <--- LIGAÇÃO
            )

            SettingsItem(icon = Icons.Default.Notifications, title = "Notifications", onClick = {})
            SettingsItem(icon = Icons.Default.Language, title = "Language", value = "English (US)", onClick = {})

            var isDarkTheme by remember { mutableStateOf(true) }
            SettingsSwitchItem(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                checked = isDarkTheme,
                onCheckedChange = { isDarkTheme = it }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Botão de Logout
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            Button(
                onClick = onLogoutClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

// ... (Os restantes componentes auxiliares: ProfileHeader, SettingsSection, etc. mantêm-se iguais)
@OptIn(InternalSerializationApi::class)
@Composable
fun ProfileHeader(profile: UserProfile) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 40.dp, bottom = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (profile.photoUrl.isNotBlank()) {
            AsyncImage(model = profile.photoUrl, contentDescription = "Profile Picture", contentScale = ContentScale.Crop, modifier = Modifier.size(100.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant))
        } else {
            Surface(modifier = Modifier.size(100.dp), shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) { Icon(Icons.Default.Person, null, modifier = Modifier.padding(20.dp), tint = Color.Gray) }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = if (profile.name.isNotBlank()) profile.name else "User", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Text(text = if (profile.email.isNotBlank()) profile.email else profile.bio.ifBlank { "No bio yet" }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
        content()
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, value: String? = null, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 24.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        if (value != null) { Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(modifier = Modifier.width(8.dp)) }
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Composable
fun SettingsSwitchItem(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}