/**
 * Jetpack Compose UI screen components for the Login screen.
 */
package com.agriflow.app.features.auth.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.agriflow.app.features.auth.AuthAction
import com.agriflow.app.features.auth.AuthEvent
import com.agriflow.app.features.auth.AuthState
import com.agriflow.app.features.auth.AuthViewModel
import com.agriflow.app.features.staff.auth.StaffLoginDialog

@Composable
fun LoginRoute(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    onForgotClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                AuthEvent.NavigateToMain -> onLoginSuccess()
                is AuthEvent.NavigateToOtp -> { /* Not applicable to Login screen */ }
                is AuthEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    LoginScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction,
        onRegisterClick = onRegisterClick,
        onForgotClick = onForgotClick,
        onStaffLoginSuccess = onLoginSuccess
    )
}

@Composable
fun LoginScreen(
    state: AuthState,
    snackbarHostState: SnackbarHostState,
    onAction: (AuthAction) -> Unit,
    onRegisterClick: () -> Unit,
    onForgotClick:() -> Unit,
    onStaffLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStaffLogin by remember { mutableStateOf(false) }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Welcome to Agriflow",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = { showStaffLogin = true }
                    )
                }
            )
            Text(
                text = "Join the future of Agriculture.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = state.loginEmail,
                onValueChange = { onAction(AuthAction.LoginEmailChanged(it)) },
                label = { Text("Email") },
                singleLine = true,
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.loginPassword,
                onValueChange = { onAction(AuthAction.LoginPasswordChanged(it)) },
                label = { Text("Password") },
                singleLine = true,
                enabled = !state.isLoading,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            state.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onAction(AuthAction.LoginSubmitted) },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Sign in")
                }
            }

            TextButton(
                onClick = onRegisterClick,
                enabled = !state.isLoading,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Create an account")
            }

            TextButton(
                onClick = onForgotClick,
                enabled = !state.isLoading,
                modifier =  Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Forgot Password ?")
            }

        }
    }

    if (showStaffLogin) {
        StaffLoginDialog(
            onDismiss = { showStaffLogin = false },
            onLoginSuccess = {
                showStaffLogin = false
                onStaffLoginSuccess()
            }
        )
    }
}
