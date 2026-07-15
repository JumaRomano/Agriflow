/**
 * Jetpack Compose UI screen components for the Payment screen.
 */
package com.agriflow.app.features.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.autofill.ContentType
import androidx.compose.ui.semantics.contentType
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentRoute(
    onNavigateBack: () -> Unit,
    onNavigateToOrders: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                PaymentEvent.NavigateBack -> onNavigateBack()
                PaymentEvent.NavigateToOrders -> onNavigateToOrders()
                is PaymentEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    PaymentScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    state: PaymentState,
    snackbarHostState: SnackbarHostState,
    onAction: (PaymentAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val mpesaGreen = Color(0xFF388E3C)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checkout Payment", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { onAction(PaymentAction.OnNavigateBack) },
                        enabled = !state.isProcessing && !state.isVerifyingPayment
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // --- 1. AMOUNT DISPLAY CARD ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Amount to Pay",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "KES ${"%,.2f".format(state.amount)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                //payment
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = mpesaGreen.copy(alpha = 0.1f)
                    ),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payment,
                            contentDescription = null,
                            tint = mpesaGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "M-Pesa",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = mpesaGreen
                        )
                    }
                }

                // --- 3. DELIVERY DETAILS ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Delivery Address",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    OutlinedTextField(
                        value = state.deliveryAddress,
                        onValueChange = { onAction(PaymentAction.OnDeliveryAddressChanged(it)) },
                        placeholder = {
                            Text(
                                text = "e.g. Nairobi",
                                // Reduces opacity so it looks like a faded hint, not actual text
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        singleLine = false,
                        maxLines = 2,
                        isError = state.deliveryAddressError != null,
                        supportingText = state.deliveryAddressError?.let { { Text(it) } },
                        enabled = !state.isProcessing && !state.isVerifyingPayment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentType = ContentType.PostalAddress }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Delivery Notes (Optional)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    OutlinedTextField(
                        value = state.deliveryNotes,
                        onValueChange = { onAction(PaymentAction.OnDeliveryNotesChanged(it)) },
                        placeholder = {
                            Text(
                                text = "e.g. Leave at the front door", // Made the hint more descriptive
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        singleLine = false,
                        maxLines = 2,
                        enabled = !state.isProcessing && !state.isVerifyingPayment,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

// --- 4. INPUT PHONE NUMBER ---
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Payment Phone Number",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    OutlinedTextField(
                        value = state.phoneNumber,
                        onValueChange = { onAction(PaymentAction.OnPhoneNumberChanged(it)) },
                        placeholder = {
                            Text(
                                text = "e.g. 0712345678",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone
                        ),
                        isError = state.phoneNumberError != null,
                        supportingText = state.phoneNumberError?.let { { Text(it) } },
                        enabled = !state.isProcessing && !state.isVerifyingPayment,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentType = ContentType.PhoneNumber }
                    )
                }

                // --- 5.PAYMENT INFORMATION ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "To Checkout",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Enter your M-Pesa PIN to approve the transaction.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.2f
                        )
                    }
                }

                if (state.errorMessage != null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (state.orderId != null) {
                            Button(
                                onClick = { onAction(PaymentAction.OnRestoreCartAndGoBack) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                if (state.isProcessing) {
                                    CircularProgressIndicator(
                                        color = MaterialTheme.colorScheme.onSecondary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        text = "Back To Cart",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // --- 6. PAY BUTTON ---
                Button(
                    onClick = { onAction(PaymentAction.OnInitiatePayment) },
                    enabled = !state.isProcessing && !state.isVerifyingPayment && state.phoneNumber.isNotBlank() && state.deliveryAddress.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = mpesaGreen,
                        contentColor = Color.White,
                        disabledContainerColor = mpesaGreen.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (state.isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Pay KES ${"%,.2f".format(state.amount)} via M-Pesa",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // --- STK PUSH WAITING DIALOG ---
            if (state.stkPushSent) {
                AlertDialog(
                    onDismissRequest = { /* Prevent dismiss by tapping outside */ },
                    title = {
                        Text(
                            text = "M-Pesa Payment",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "An M-Pesa prompt has been sent to ${state.phoneNumber}.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Enter your M-Pesa PIN to complete the payment.",
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = mpesaGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            CircularProgressIndicator(color = mpesaGreen)
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = { onAction(PaymentAction.OnDismissSuccessDialog) }
                        ) {
                            Text("I Have Paid", color = mpesaGreen, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // --- VERIFYING PAYMENT DIALOG ---
            if (state.isVerifyingPayment) {
                AlertDialog(
                    onDismissRequest = {},
                    title = {
                        Text(
                            text = "Verifying Payment",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Please wait while we confirm your payment.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            CircularProgressIndicator(color = mpesaGreen)
                        }
                    },
                    confirmButton = {}
                )
            }
        }
    }
}
