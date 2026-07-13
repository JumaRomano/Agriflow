/**
 * Jetpack Compose UI screen components for the Register screen.
 */
package com.agriflow.app.features.auth.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.agriflow.app.features.auth.AuthAction
import com.agriflow.app.features.auth.AuthEvent
import com.agriflow.app.features.auth.AuthState
import com.agriflow.app.features.auth.AuthViewModel


@Composable
fun RegisterRoute(
    onNavigateToOtp: (String, String) -> Unit,
    onRegisterSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.NavigateToOtp -> onNavigateToOtp(event.email, event.type)
                AuthEvent.NavigateToMain -> onRegisterSuccess()
                AuthEvent.NavigateToStaffDashboard -> { /* Not applicable to Register screen */ }
                is AuthEvent.NavigateToChangePassword -> { /* Not applicable to Register screen */ }
                is AuthEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    RegisterScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onLoginClick = onLoginClick
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(
    state: AuthState,
    snackbarHostState: SnackbarHostState,
    onAction: (AuthAction) -> Unit,
    onLoginClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
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
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                StaggeredEntrance(index = 0) {
                    Column {
                        Text(
                            text = "Create account",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "Join Agriflow the future of Agriculture.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                StaggeredEntrance(index = 1, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.registerusername,
                        onValueChange = { onAction(AuthAction.RegisterusernameChanged(it)) },
                        label = { Text("Username") },
                        singleLine = true,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                StaggeredEntrance(index = 2, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.registerfirstName,
                        onValueChange = { onAction(AuthAction.RegisterfirstNameChanged(it)) },
                        label = { Text("First name") },
                        singleLine = true,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                StaggeredEntrance(index = 3, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.registersurName,
                        onValueChange = { onAction(AuthAction.RegistersurNameChanged(it)) },
                        label = { Text("Surname") },
                        singleLine = true,
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                StaggeredEntrance(index = 4, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.registerEmail,
                        onValueChange = { onAction(AuthAction.RegisterEmailChanged(it)) },
                        label = { Text("Email") },
                        singleLine = true,
                        enabled = !state.isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                StaggeredEntrance(index = 5, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.registerPhoneNumber,
                        onValueChange = { onAction(AuthAction.RegisterPhoneNumberChanged(it)) },
                        label = { Text("Phone number") },
                        singleLine = true,
                        enabled = !state.isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                StaggeredEntrance(index = 6, modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.registerPassword,
                        onValueChange = { onAction(AuthAction.RegisterPasswordChanged(it)) },
                        label = { Text("Password") },
                        singleLine = true,
                        enabled = !state.isLoading,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                StaggeredEntrance(index = 7, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = state.registerTermsAccepted,
                            onCheckedChange = {
                                onAction(AuthAction.RegisterTermsAcceptedChanged(it))
                            },
                            enabled = !state.isLoading
                        )
                        Text(
                            text = "I agree to the Terms of Service and Privacy Policy.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                state.errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(12.dp))
                    StaggeredEntrance(index = 8) {
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                StaggeredEntrance(index = 8, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { onAction(AuthAction.RegisterSubmitted) },
                        enabled = !state.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AnimatedContent(targetState = state.isLoading, label = "RegisterLoading") { isLoading ->
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text("Create account")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                StaggeredEntrance(index = 9, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    TextButton(
                        onClick = onLoginClick,
                        enabled = !state.isLoading
                    ) {
                        Text("Already have an account? Log in")
                    }
                }
            }
        }
    }
}




