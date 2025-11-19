package com.example.eventify.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Classes/objetos de dados para BottomNav
sealed class BottomNavItem(val route: String, val icon: ImageVector, val label: String) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Explore : BottomNavItem("explore", Icons.Default.Search, "Explore")
    object MyEvents : BottomNavItem("myevents", Icons.AutoMirrored.Filled.List, "My Events")
    object Profile : BottomNavItem("profile", Icons.Default.AccountCircle, "Profile")
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Explore,
    BottomNavItem.MyEvents,
    BottomNavItem.Profile
)

@Composable
fun EventifyBottomBar(
    onNavigate: (String) -> Unit
) {
    var selectedItem by remember { mutableIntStateOf(0) }

    NavigationBar {
        bottomNavItems.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                selected = selectedItem == index,
                onClick = {
                    selectedItem = index
                    onNavigate(item.route)
                }
            )
        }
    }
}

@Composable
fun IconButtonWithBadge(
    icon: ImageVector,
    badgeCount: Int? = null,
    onClick: () -> Unit,
    contentDescription: String? = null
) {
    IconButton(onClick = onClick) {
        if (badgeCount != null && badgeCount > 0) {
            BadgedBox(
                badge = { Badge { Text(text = badgeCount.toString()) } }
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription
                )
            }
        } else {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription
            )
        }
    }
}