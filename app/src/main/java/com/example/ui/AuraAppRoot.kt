package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.components.AuraMiniPlayerBar
import com.example.core.designsystem.*
import com.example.core.state.*
import com.example.feature.auth.ForgotPasswordScreen
import com.example.feature.auth.LoginScreen
import com.example.feature.auth.SignUpScreen
import com.example.feature.downloads.DownloadsScreen
import com.example.feature.home.HomeScreen
import com.example.feature.library.LibraryScreen
import com.example.feature.player.PlayerScreen
import com.example.feature.search.SearchScreen
import com.example.feature.settings.SettingsScreen
import com.example.feature.user.ProfileScreen
import com.example.navigation.AuraRoute

@Composable
fun AuraAppRoot(
    themeState: ThemeState = viewModel(),
    userState: UserState = viewModel(),
    settingsState: SettingsState = viewModel(),
    playerState: PlayerState = viewModel(),
    downloadsState: DownloadsState = viewModel(),
    libraryState: LibraryState = viewModel(),
    searchState: SearchState = viewModel()
) {
    val themeMode by themeState.themeMode.collectAsState()
    val session by userState.session.collectAsState()
    val playerData by playerState.playerData.collectAsState()

    var currentRoute by remember { mutableStateOf(AuraRoute.HOME) }
    var showFullPlayer by remember { mutableStateOf(false) }

    // Screen width breakpoint calculation
    val configuration = LocalConfiguration.current
    val windowSizeClass = when {
        configuration.screenWidthDp >= 840 -> WindowSizeClass.EXPANDED
        configuration.screenWidthDp >= 600 -> WindowSizeClass.MEDIUM
        else -> WindowSizeClass.COMPACT
    }

    AuraTheme(themeMode = themeMode) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            if (showFullPlayer && playerData.currentSong != null) {
                PlayerScreen(
                    onDismiss = { showFullPlayer = false },
                    playerState = playerState
                )
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Side Navigation Rail for Medium / Expanded screens (Tablets & Desktop)
                    if (windowSizeClass != WindowSizeClass.COMPACT) {
                        AuraSideNavigationRail(
                            currentRoute = currentRoute,
                            onRouteSelected = { currentRoute = it }
                        )
                    }

                    // Main Screen Viewport
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                ) {
                                    // Persistent Mini Player
                                    if (playerData.currentSong != null && currentRoute != AuraRoute.LOGIN && currentRoute != AuraRoute.SIGN_UP && currentRoute != AuraRoute.FORGOT_PASSWORD) {
                                        val dur = if (playerData.durationMs > 0) playerData.durationMs else 1L
                                        val progressFrac = (playerData.progressMs.toFloat() / dur.toFloat()).coerceIn(0f, 1f)
                                        AuraMiniPlayerBar(
                                            songTitle = playerData.currentSong?.title ?: "",
                                            artistName = playerData.currentSong?.artistName ?: "",
                                            coverUrl = playerData.currentSong?.coverUrl ?: "",
                                            isPlaying = playerData.isPlaying,
                                            progressFraction = progressFrac,
                                            onPlayPauseClick = playerState::togglePlayPause,
                                            onSkipClick = playerState::skipNext,
                                            onPreviousClick = playerState::skipPrevious,
                                            onBarClick = { showFullPlayer = true }
                                        )
                                    }

                                    // Bottom Navigation Bar for Mobile Compact screens
                                    if (windowSizeClass == WindowSizeClass.COMPACT && currentRoute != AuraRoute.LOGIN && currentRoute != AuraRoute.SIGN_UP && currentRoute != AuraRoute.FORGOT_PASSWORD) {
                                        AuraBottomNavigationBar(
                                            currentRoute = currentRoute,
                                            onRouteSelected = { currentRoute = it }
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (currentRoute) {
                                    AuraRoute.HOME -> HomeScreen(
                                        onSongSelect = {
                                            playerState.playSong(it)
                                            showFullPlayer = true
                                        },
                                        onAlbumSelect = {},
                                        onArtistSelect = {}
                                    )
                                    AuraRoute.SEARCH -> SearchScreen(
                                        onSongSelect = { song ->
                                            playerState.playSong(song)
                                            showFullPlayer = true
                                        },
                                        searchState = searchState
                                    )
                                    AuraRoute.LIBRARY -> LibraryScreen(
                                        onSongSelect = { song ->
                                            playerState.playSong(song)
                                            showFullPlayer = true
                                        },
                                        libraryState = libraryState
                                    )
                                    AuraRoute.DOWNLOADS -> DownloadsScreen(
                                        onSongSelect = { song ->
                                            playerState.playSong(song)
                                            showFullPlayer = true
                                        },
                                        downloadsState = downloadsState
                                    )
                                    AuraRoute.PROFILE -> ProfileScreen(
                                        userState = userState,
                                        onLogout = {
                                            userState.logout()
                                            currentRoute = AuraRoute.LOGIN
                                        }
                                    )
                                    AuraRoute.SETTINGS -> SettingsScreen(
                                        themeState = themeState,
                                        settingsState = settingsState
                                    )
                                    AuraRoute.LOGIN -> LoginScreen(
                                        onNavigateToSignUp = { currentRoute = AuraRoute.SIGN_UP },
                                        onNavigateToForgotPassword = { currentRoute = AuraRoute.FORGOT_PASSWORD },
                                        onLoginSuccess = { currentRoute = AuraRoute.HOME }
                                    )
                                    AuraRoute.SIGN_UP -> SignUpScreen(
                                        onNavigateToLogin = { currentRoute = AuraRoute.LOGIN },
                                        onSignUpSuccess = { currentRoute = AuraRoute.HOME }
                                    )
                                    AuraRoute.FORGOT_PASSWORD -> ForgotPasswordScreen(
                                        onNavigateBack = { currentRoute = AuraRoute.LOGIN }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuraBottomNavigationBar(
    currentRoute: AuraRoute,
    onRouteSelected: (AuraRoute) -> Unit
) {
    val navRoutes = listOf(
        AuraRoute.HOME,
        AuraRoute.SEARCH,
        AuraRoute.LIBRARY,
        AuraRoute.DOWNLOADS,
        AuraRoute.PROFILE,
        AuraRoute.SETTINGS
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = AuraElevation.Glass,
        modifier = Modifier.testTag("aura_bottom_nav_bar")
    ) {
        navRoutes.forEach { route ->
            val isSelected = currentRoute == route
            NavigationBarItem(
                selected = isSelected,
                onClick = { onRouteSelected(route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) route.selectedIcon else route.unselectedIcon,
                        contentDescription = route.title
                    )
                },
                label = { Text(text = route.title, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AuraColors.NeonCyan,
                    indicatorColor = AuraColors.ElectricPurple.copy(alpha = 0.3f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
private fun AuraSideNavigationRail(
    currentRoute: AuraRoute,
    onRouteSelected: (AuraRoute) -> Unit
) {
    val navRoutes = listOf(
        AuraRoute.HOME,
        AuraRoute.SEARCH,
        AuraRoute.LIBRARY,
        AuraRoute.DOWNLOADS,
        AuraRoute.PROFILE,
        AuraRoute.SETTINGS
    )

    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("aura_side_nav_rail")
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        navRoutes.forEach { route ->
            val isSelected = currentRoute == route
            NavigationRailItem(
                selected = isSelected,
                onClick = { onRouteSelected(route) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) route.selectedIcon else route.unselectedIcon,
                        contentDescription = route.title
                    )
                },
                label = { Text(text = route.title, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = AuraColors.NeonCyan,
                    indicatorColor = AuraColors.ElectricPurple.copy(alpha = 0.3f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
