package com.example.eventify.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.eventify.di.AppModule
import com.example.eventify.model.Badge
import com.example.eventify.model.UserProfile
import kotlinx.serialization.InternalSerializationApi

// Cores das Medalhas
private val Bronze = Color(0xFFCD7F32)
private val Silver = Color(0xFFC0C0C0)
private val Gold = Color(0xFFFFD700)

@OptIn(InternalSerializationApi::class)
@Composable
fun ProfileScreen(
    onLogoutClick: () -> Unit,
    onOrganizerClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onInterestsClick: () -> Unit
) {
    val viewModel = remember { AppModule.provideProfileViewModel() }
    val profile by viewModel.profile.collectAsState()
    val badges by viewModel.badges.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        ProfileHeader(profile)

        // --- SECÇÃO: CONQUISTAS (BADGES) ---
        if (badges.isNotEmpty()) {
            BadgesSection(badges)
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }

        // Botão para Modo Organizador
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            Button(
                onClick = onOrganizerClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Mudar para Modo Organizador", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }

        SettingsSection(title = "Conta") {
            SettingsItem(Icons.Default.Person, "Informações Pessoais", onClick = onEditProfileClick)
            SettingsItem(Icons.Default.Payment, "Métodos de Pagamento", onClick = {})
            SettingsItem(Icons.Default.Security, "Segurança e Password", onClick = {})
        }

        SettingsSection(title = "Preferências") {
            SettingsItem(Icons.Default.Favorite, "Os Meus Interesses", onClick = onInterestsClick)
            SettingsItem(Icons.Default.Notifications, "Notificações", onClick = {})
            SettingsItem(Icons.Default.Language, "Idioma", value = "Português (PT)", onClick = {})

            var isDarkTheme by remember { mutableStateOf(true) }
            SettingsSwitchItem(Icons.Default.DarkMode, "Modo Escuro", checked = isDarkTheme, onCheckedChange = { isDarkTheme = it })
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Botão Sair
        Box(modifier = Modifier.padding(horizontal = 24.dp)) {
            OutlinedButton(
                onClick = onLogoutClick,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Terminar Sessão", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun BadgesSection(badges: List<Badge>) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            "Conquistas e Medalhas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            badges.forEach { badge ->
                BadgeItem(badge)
            }
        }
    }
}

@Composable
fun BadgeItem(badge: Badge) {
    val (color, label) = when(badge) {
        Badge.ROOKIE -> Bronze to "Iniciante"
        Badge.SOCIAL -> Silver to "Socialite"
        Badge.VIP -> Gold to "VIP"
    }

    val icon = when(badge) {
        Badge.ROOKIE -> Icons.Default.ConfirmationNumber
        Badge.SOCIAL -> Icons.Default.Comment
        Badge.VIP -> Icons.Default.EmojiEvents
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(85.dp)) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(color.copy(alpha = 0.15f), CircleShape)
                .border(2.dp, color.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = color)
    }
}

@OptIn(InternalSerializationApi::class)
@Composable
fun ProfileHeader(profile: UserProfile) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 60.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (profile.photoUrl.isNotBlank()) {
            AsyncImage(
                model = profile.photoUrl,
                contentDescription = "Foto de Perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(110.dp).clip(CircleShape).border(3.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), CircleShape)
            )
        } else {
            Surface(
                modifier = Modifier.size(110.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Icon(Icons.Default.Person, null, modifier = Modifier.padding(25.dp), tint = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = if (profile.name.isNotBlank()) profile.name else "Utilizador",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = if (profile.email.isNotBlank()) profile.email else profile.bio.ifBlank { "Sem biografia definida" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        content()
    }
}

@Composable
fun SettingsItem(icon: ImageVector, title: String, value: String? = null, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        if (value != null) {
            Text(value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(14.dp))
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}

@Composable
fun SettingsSwitchItem(icon: ImageVector, title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}