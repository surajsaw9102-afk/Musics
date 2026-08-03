package com.example.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class AuraRoute(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    HOME("Home", Icons.Filled.Home, Icons.Outlined.Home),
    SEARCH("Search", Icons.Filled.Search, Icons.Outlined.Search),
    LIBRARY("Library", Icons.Filled.LibraryMusic, Icons.Outlined.LibraryMusic),
    DOWNLOADS("Downloads", Icons.Filled.CloudDownload, Icons.Outlined.CloudDownload),
    PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.Person),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings),
    LOGIN("Login", Icons.Filled.Lock, Icons.Outlined.Lock),
    SIGN_UP("Sign Up", Icons.Filled.PersonAdd, Icons.Outlined.PersonAdd),
    FORGOT_PASSWORD("Forgot Password", Icons.Filled.Key, Icons.Outlined.Key)
}
