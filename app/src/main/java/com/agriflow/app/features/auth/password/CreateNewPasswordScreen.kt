/**
 * Jetpack Compose UI screen components for the CreateNewPassword screen.
 */
package com.agriflow.app.features.auth.password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.AnimatedContent
import com.agriflow.app.features.auth.ui.AuthBackgroundOrnaments
import com.agriflow.app.features.auth.ui.StaggeredEntrance
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun CreateNewPasswordRoute(
    onResetSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: CreateNewPasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateNewPasswordEvent.NavigateToLogin -> {
                    onResetSuccess()
                }
                is CreateNewPasswordEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    CreateNewPasswordScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNewPasswordScreen(
    state: CreateNewPasswordState,
    snackbarHostState: SnackbarHostState,
    onAction: (CreateNewPasswordAction) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        enabled = !state.isLoading
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AuthBackgroundOrnaments()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                StaggeredEntrance(index = 0) {
                    Column {
                        Text(
                            text = "New Password",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Please enter your new secure password to reset your login credentials.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                StaggeredEntrance(index = 1, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.newPassword,
                        onValueChange = { onAction(CreateNewPasswordAction.OnNewPasswordChanged(it)) },
                        label = { Text("New Password") },
                        singleLine = true,
                        enabled = !state.isLoading,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentType = ContentType.NewPassword }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                StaggeredEntrance(index = 2, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.confirmPassword,
                        onValueChange = { onAction(CreateNewPasswordAction.OnConfirmPasswordChanged(it)) },
                        label = { Text("Confirm Password") },
                        singleLine = true,
                        enabled = !state.isLoading,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentType = ContentType.NewPassword }
                    )
                }

                state.error?.let { error ->
                    Spacer(modifier = Modifier.height(12.dp))
                    StaggeredEntrance(index = 3) {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                StaggeredEntrance(index = 3, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onAction(CreateNewPasswordAction.SubmitClicked) },
                        enabled = !state.isLoading && state.newPassword.isNotBlank() && state.confirmPassword.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AnimatedContent(targetState = state.isLoading, label = "ResetLoading") { isLoading ->
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text("Reset Password")
                            }
                        }
                    }
                }
            }
        }
    }
}
