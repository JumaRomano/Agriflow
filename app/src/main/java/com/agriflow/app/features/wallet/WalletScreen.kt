package com.agriflow.app.features.wallet

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.ArrowCircleDown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletRoute(
    onNavigateBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                WalletEvent.NavigateBack -> onNavigateBack()
                is WalletEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    WalletScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(
    state: WalletState,
    onAction: (WalletAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Wallet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onAction(WalletAction.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onAction(WalletAction.RefreshWallet) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Available Balance Card
                AvailableBalanceCard(balance = state.balance)

                Spacer(modifier = Modifier.height(16.dp))

                // "Withdraw Funds" primary action button
                Button(
                    onClick = { onAction(WalletAction.ShowWithdrawDialog(true)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Withdraw Funds",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pending Balance Section
                PendingBalanceSection(pendingBalance = state.pendingBalance)

                Spacer(modifier = Modifier.height(32.dp))

                // Transactions Header
                Text(
                    text = "Transaction History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Transactions List
                if (state.transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No transactions recorded.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(state.transactions) { transaction ->
                            TransactionItem(transaction = transaction)
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }

            // Global Loading Indicator overlay
            if (state.isLoading && !state.isWithdrawDialogVisible) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Withdrawal Dialog Form overlay
        if (state.isWithdrawDialogVisible) {
            WithdrawalDialog(
                state = state,
                onAction = onAction
            )
        }
    }
}

@Composable
fun AvailableBalanceCard(balance: Double) {
    val formattedBalance = remember(balance) {
        "KES %,.2f".format(Locale.US, balance)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSystemInDarkTheme()) {
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Available Balance",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formattedBalance,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PendingBalanceSection(pendingBalance: Double) {
    val formattedPending = remember(pendingBalance) {
        "KES %,.2f".format(Locale.US, pendingBalance)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = "Pending Balance",
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formattedPending,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Funds are released to available balance once products are successfully delivered.",
            color = if (isSystemInDarkTheme()) {
                Color.Gray
            } else {
                Color(0xFF757575)
            },
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 16.sp
        )
    }
}

@Composable
fun TransactionItem(transaction: WalletTransaction) {
    val configuration = LocalConfiguration.current
    val locale = remember(configuration) { configuration.locales[0] }

    val formattedDate = remember(transaction.timestamp, locale) {
        SimpleDateFormat("MMM dd, yyyy • hh:mm a", locale).format(Date(transaction.timestamp))
    }

    val isRevenue = transaction.type == TransactionType.REVENUE

    val formattedAmount = remember(transaction.amount, transaction.type) {
        if (isRevenue) {
            "+KES %,.2f".format(Locale.US, transaction.amount)
        } else {
            "-KES %,.2f".format(Locale.US, transaction.amount)
        }
    }

    val badgeColor = if (isRevenue) {
        Color(0xFF2E7D32) // Professional green for positive inflow
    } else {
        Color(0xFFC62828) // Professional red for negative outflow
    }

    val badgeIcon = if (isRevenue) {
        Icons.AutoMirrored.Filled.TrendingUp
    } else {
        Icons.AutoMirrored.Filled.TrendingDown
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon Badge with subtle tint background
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(badgeColor.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = badgeIcon,
                contentDescription = null,
                tint = badgeColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }

        Text(
            text = formattedAmount,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = badgeColor
        )
    }
}

@Composable
fun WithdrawalDialog(
    state: WalletState,
    onAction: (WalletAction) -> Unit
) {
    Dialog(onDismissRequest = { if (!state.isLoading) onAction(WalletAction.ShowWithdrawDialog(false)) }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                if (state.isOtpSent) {
                    Text(
                        text = "Verify Withdrawal",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "An OTP has been sent to your registered email. Enter it below to complete the withdrawal of KES ${state.withdrawAmount} to M-Pesa ${state.withdrawMpesaNumber}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // OTP Input
                    OutlinedTextField(
                        value = state.otpCode,
                        onValueChange = { onAction(WalletAction.OtpCodeChanged(it)) },
                        label = { Text("Verification Code") },
                        placeholder = { Text("Enter 6-digit OTP") },
                        isError = state.otpError != null,
                        supportingText = { state.otpError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onAction(WalletAction.ResendOtp) },
                            enabled = !state.isLoading
                        ) {
                            Text("Resend OTP")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onAction(WalletAction.GoBackToWithdrawDetails) },
                            enabled = !state.isLoading
                        ) {
                            Text("Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onAction(WalletAction.VerifyAndWithdraw) },
                            enabled = !state.isLoading,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Verify & Withdraw")
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Withdraw to M-Pesa",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Withdrawal limit: Min KES 10.00",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount Input
                    OutlinedTextField(
                        value = state.withdrawAmount,
                        onValueChange = { onAction(WalletAction.WithdrawAmountChanged(it)) },
                        label = { Text("Amount (KES)") },
                        isError = state.withdrawAmountError != null,
                        supportingText = { state.withdrawAmountError?.let { Text(it) } ?: Text("Minimum: KES 10.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // M-Pesa Phone Number Input
                    if (state.defaultMpesaNumber != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Checkbox(
                                checked = state.useDefaultMpesa,
                                onCheckedChange = { onAction(WalletAction.ToggleUseDefaultMpesa(it)) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Use default M-Pesa number (${state.defaultMpesaNumber})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    OutlinedTextField(
                        value = state.withdrawMpesaNumber,
                        onValueChange = { onAction(WalletAction.WithdrawMpesaNumberChanged(it)) },
                        label = { Text("M-Pesa Phone Number") },
                        placeholder = { Text("e.g. 0712345678") },
                        enabled = !state.useDefaultMpesa,
                        isError = state.withdrawMpesaNumberError != null,
                        supportingText = { state.withdrawMpesaNumberError?.let { Text(it) } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onAction(WalletAction.ShowWithdrawDialog(false)) },
                            enabled = !state.isLoading
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onAction(WalletAction.SubmitWithdrawal) },
                            enabled = !state.isLoading,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Withdraw")
                            }
                        }
                    }
                }
            }
        }
    }
}
