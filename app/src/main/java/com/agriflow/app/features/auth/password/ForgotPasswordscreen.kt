/**
 * Jetpack Compose UI screen components for the ForgotPassword screen.
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun ForgotPasswordRoute(
    onNavigateToOtp: (String, String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ForgotPasswordViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is ForgotPasswordEvent.NavigateToOtp -> {
                    onNavigateToOtp(event.email, event.type)
                }
                is ForgotPasswordEvent.ShowMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    ForgotPasswordScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    state: ForgotPasswordState,
    snackbarHostState: SnackbarHostState,
    onAction: (ForgotPasswordAction) -> Unit,
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
                            text = "Forgot Password?",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Enter your email address and we will send you an OTP code to verify your identity.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                StaggeredEntrance(index = 1, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { onAction(ForgotPasswordAction.OnEmailChanged(it)) },
                        label = { Text("Email Address") },
                        singleLine = true,
                        enabled = !state.isLoading,
                        isError = state.emailError != null,
                        supportingText = state.emailError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentType = ContentType.EmailAddress }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                StaggeredEntrance(index = 2, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onAction(ForgotPasswordAction.SendOtpClicked) },
                        enabled = !state.isLoading && state.email.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AnimatedContent(targetState = state.isLoading, label = "ForgotLoading") { isLoading ->
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text("Send OTP Verification")
                            }
                        }
                    }
                }
            }
        }
    }
}