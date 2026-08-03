package com.example.feature.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.components.*

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    viewModel: AuthViewModel = viewModel(),
    testTag: String = "forgot_password_screen"
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .testTag(testTag)
            .fillMaxSize()
            .padding(24.dp)
    ) {
        AuraIconButton(
            icon = Icons.Default.ArrowBack,
            contentDescription = "Back",
            onClick = onNavigateBack,
            size = 40.dp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Reset Password",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your registered email and we'll send password recovery instructions.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        AuraTextField(
            value = uiState.email,
            onValueChange = viewModel::onEmailChange,
            placeholder = "Email Address",
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = uiState.errorMessage != null,
            errorMessage = uiState.errorMessage
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (uiState.isSuccess) {
            AuraToastBanner(
                message = "Password reset link sent to your email!",
                visible = true,
                type = AuraToastType.SUCCESS
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AuraCircularProgress()
            }
        } else {
            AuraButton(
                text = "Send Reset Link",
                onClick = viewModel::resetPassword,
                modifier = Modifier.fillMaxWidth(),
                variant = AuraButtonVariant.PRIMARY_GRADIENT
            )
        }
    }
}
