package com.example.feature.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.components.*
import com.example.core.designsystem.AuraColors
import com.example.core.state.UserState

@Composable
fun ProfileScreen(
    userState: UserState,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
    testTag: String = "profile_screen"
) {
    val session by userState.session.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuraSectionHeader(
            title = "User Profile",
            subtitle = "Manage your free account details"
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuraAvatar(
            photoUrl = session.user?.photoUrl,
            size = 96.dp,
            showFreeBadge = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = session.user?.displayName ?: "Music Fan",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = session.user?.username ?: "@listener",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        AuraFreeBadge()

        Spacer(modifier = Modifier.height(28.dp))

        if (uiState.isSaved) {
            AuraToastBanner(
                message = "Profile updated successfully!",
                visible = true,
                type = AuraToastType.SUCCESS
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Account Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                AuraIconButton(
                    icon = Icons.Default.Edit,
                    contentDescription = "Edit Profile",
                    onClick = viewModel::toggleEdit,
                    size = 36.dp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.isEditing) {
                AuraTextField(
                    value = uiState.displayName,
                    onValueChange = viewModel::updateName,
                    placeholder = "Display Name"
                )
                Spacer(modifier = Modifier.height(12.dp))
                AuraTextField(
                    value = uiState.username,
                    onValueChange = viewModel::updateUsername,
                    placeholder = "Username"
                )
                Spacer(modifier = Modifier.height(12.dp))
                AuraTextField(
                    value = uiState.email,
                    onValueChange = viewModel::updateEmail,
                    placeholder = "Email Address"
                )
                Spacer(modifier = Modifier.height(16.dp))
                AuraButton(
                    text = "Save Changes",
                    onClick = { viewModel.saveProfile(userState) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = AuraButtonVariant.PRIMARY_GRADIENT
                )
            } else {
                ProfileDetailRow("Name", session.user?.displayName ?: "")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ProfileDetailRow("Username", session.user?.username ?: "")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ProfileDetailRow("Email", session.user?.email ?: "")
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ProfileDetailRow("Plan", "100% Free Lifetime")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        AuraGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Regional Preferences",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            ProfileDetailRow("Language", session.user?.language ?: "English")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ProfileDetailRow("Country / Region", session.user?.country ?: "United States")
        }

        Spacer(modifier = Modifier.height(28.dp))

        AuraButton(
            text = "Sign Out",
            onClick = onLogout,
            icon = Icons.Default.Logout,
            modifier = Modifier.fillMaxWidth(),
            variant = AuraButtonVariant.OUTLINED
        )
    }
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
