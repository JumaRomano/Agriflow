/**
 * Jetpack Compose UI screen components for the Profile screen.
 */
package com.agriflow.app.features.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.compose.AsyncImage
import com.agriflow.app.features.auth.UserRole


@Composable
fun ProfileRoute(
    onLogoutSuccess: () -> Unit,
    onNavigateToUpgrade: (UserRole) -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToWallet: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Trigger profile details refresh every time the user enters the screen
    LaunchedEffect(Unit) {
        viewModel.onAction(ProfileAction.OnRefreshProfile)
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                ProfileEvent.MapsToLogin -> onLogoutSuccess()
                is ProfileEvent.NavigateToRoleUpgrade -> onNavigateToUpgrade(event.role)
                is ProfileEvent.ShowToast -> {
                    android.widget.Toast.makeText(context, event.message, android.widget.Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    ProfileScreen(
        state = state,
        onAction = viewModel::onAction,
        onUpgradeClick = { onNavigateToUpgrade(UserRole.BUYER) },
        onEditProfileClick = onNavigateToEditProfile,
        onPaymentMethodsClick = onNavigateToPaymentMethods,
        onOrdersClick = onNavigateToOrders,
        onWalletClick = onNavigateToWallet
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    state: ProfileState,
    onAction: (ProfileAction) -> Unit,
    onUpgradeClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onPaymentMethodsClick: () -> Unit,
    onOrdersClick: () -> Unit,
    onWalletClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val isBusinessRole = state.role == UserRole.FARMER || state.role == UserRole.SUPPLIER

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account", fontWeight = FontWeight.Bold) },
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // ── Avatar / Business Logo ─────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    val logoToShow = if (isBusinessRole) state.businessLogoUrl else state.profilePicture
                    if (!logoToShow.isNullOrBlank()) {
                        AsyncImage(
                            model = logoToShow,
                            contentDescription = if (isBusinessRole) "Business Logo" else "User Profile Picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = if (isBusinessRole) Icons.Default.Business else Icons.Default.Person,
                            contentDescription = "Avatar",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display business name for business roles, otherwise personal name
                Text(
                    text = if (isBusinessRole && !state.businessName.isNullOrBlank())
                        state.businessName else state.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isBusinessRole && !state.businessEmail.isNullOrBlank())
                        state.businessEmail else state.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Role badge + approval status chip (for business roles)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = state.role.name,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    if (isBusinessRole && !state.businessApprovalStatus.isNullOrBlank()) {
                        val isApproved = state.businessApprovalStatus.equals("APPROVED", ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isApproved) Color(0xFF1B5E20).copy(alpha = 0.15f)
                                    else Color(0xFFE65100).copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isApproved) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = state.businessApprovalStatus.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isApproved) Color(0xFF2E7D32) else Color(0xFFBF360C)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Business Info Card (FARMER / SUPPLIER only) ────────────
                if (isBusinessRole) {
                    BusinessInfoCard(state = state)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // ── Menu Items ─────────────────────────────────────────────
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    val menuItems = remember(state.role) {
                        buildList {
                            // Edit Profile only for BUYER accounts
                            if (!isBusinessRole) {
                                add(Triple("Edit Profile", Icons.Default.Edit, onEditProfileClick))
                            }
                           // add(Triple("Payment Methods", Icons.Default.CreditCard, onPaymentMethodsClick))
                            add(Triple("Order History", Icons.Default.History, onOrdersClick))
                            if (isBusinessRole) {
                                add(Triple("Wallet", Icons.Default.AccountBalanceWallet, onWalletClick))
                            }
                        }
                    }

                    menuItems.forEachIndexed { index, item ->
                        ListItem(
                            headlineContent = { Text(item.first) },
                            leadingContent = {
                                Icon(
                                    imageVector = item.second,
                                    contentDescription = item.first,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            trailingContent = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { item.third() }
                        )
                        if (index < menuItems.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                thickness = 1.dp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                val buttonText = if (isBusinessRole) "Switch to Buyer" else "Switch Account"

                OutlinedButton(
                    onClick = { onAction(ProfileAction.OnSwitchAccountClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = buttonText,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buttonText,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { onAction(ProfileAction.OnLogoutClicked) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isLoading,
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.error
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Out",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Loading overlay
            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

/**
 * Card showing live business info from /api/businesses/me.
 * Rendered only for FARMER and SUPPLIER roles.
 */
@Composable
private fun BusinessInfoCard(state: ProfileState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Business Profile",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            if (!state.businessPhone.isNullOrBlank()) {
                BusinessInfoRow(
                    icon = {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = "Phone",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = "Phone",
                    value = state.businessPhone
                )
            }
            if (!state.businessEmail.isNullOrBlank()) {
                BusinessInfoRow(
                    icon = {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = "Email",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = "Email",
                    value = state.businessEmail
                )
            }
            if (!state.businessCounty.isNullOrBlank()) {
                BusinessInfoRow(
                    icon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "County",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = "County",
                    value = state.businessCounty
                )
            }
            if (!state.businessJoinDate.isNullOrBlank()) {

                val displayDate = state.businessJoinDate.substringBefore("T").ifBlank { state.businessJoinDate }
                BusinessInfoRow(
                    icon = {
                        Icon(
                            Icons.Default.Business,
                            contentDescription = "Joined",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = "Joined",
                    value = displayDate
                )
            }
        }
    }
}

@Composable
private fun BusinessInfoRow(
    icon: @Composable () -> Unit,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        icon()
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
