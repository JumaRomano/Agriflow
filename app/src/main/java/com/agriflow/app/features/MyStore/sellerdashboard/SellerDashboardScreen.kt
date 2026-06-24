/**
 * Jetpack Compose UI screen components for the SellerDashboard screen.
 */
package com.agriflow.app.features.MyStore.sellerdashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyStoreRoute(
    onNavigateToMyStore: () -> Unit,
    onNavigateToAddProduct: () -> Unit,
    onNavigateToOrders: (String) -> Unit,
    viewModel: SellerDashboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is SellerDashboardEvent.NavigateToExportReport -> {
                    // Navigate to export screen or handle export action
                }
                is SellerDashboardEvent.NavigateToOrderDetail -> {
                    onNavigateToOrders(event.orderId)
                }
                is SellerDashboardEvent.ShowSnackbarMessage -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                SellerDashboardEvent.NavigateToAddProduct -> {
                    onNavigateToAddProduct()
                }
            }
        }
    }

    SellerDashboardScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerDashboardScreen(
    state: SellerDashboardState,
    snackbarHostState: SnackbarHostState,
    onAction: (SellerDashboardAction) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Store Dashboard", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(SellerDashboardAction.OnAddProductClicked) },
                icon = { Icon(Icons.Default.Add, contentDescription = "My Products") },
                text = { Text("My Products") },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Header Title Area
            item {
                Column {
                    Text(
                        text = "Overview",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Monitor your store sales, orders and inventory levels",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Filter: ",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MonthFilterDropdown(
                            selectedFilter = state.selectedMonthFilter,
                            monthlyBreakdown = state.monthlyBreakdown,
                            onMonthSelected = { month ->
                                onAction(SellerDashboardAction.OnMonthFilterSelected(month))
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // KPI Grid (Responsive grid layout for mobile screen size using weight)
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        KpiCard(
                            title = if (state.selectedMonthFilter == "All Time") "Total Revenue" else "${state.selectedMonthFilter} Revenue",
                            value = state.displayRevenue,
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            iconTint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "This Month Revenue",
                            value = state.thisMonthRevenue,
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            iconTint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        KpiCard(
                            title = if (state.selectedMonthFilter == "All Time") "Total Orders" else "${state.selectedMonthFilter} Orders",
                            value = state.displayOrders.toString(),
                            icon = Icons.Default.Storefront,
                            iconTint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "Pending Orders",
                            value = state.pendingOrders.toString(),
                            icon = Icons.Default.Schedule,
                            iconTint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        KpiCard(
                            title = "Delivered Orders",
                            value = state.deliveredOrders.toString(),
                            icon = Icons.Default.Storefront,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        KpiCard(
                            title = "Active Products",
                            value = state.activeListings.toString(),
                            icon = Icons.Default.Storefront,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                }
            }

            // Monthly Breakdown Section
            if (state.monthlyBreakdown.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Monthly Performance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                itemsIndexed(
                    items = state.monthlyBreakdown
                ) { index, item ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = formatMonthOnly(item.monthLabel),
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        supportingContent = {
                            Text(
                                text = "${item.orderCount ?: 0} orders",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingContent = {
                            Text(
                                text = "Ksh ${String.format("%.2f", item.revenue ?: 0.0)}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        colors = ListItemDefaults.colors(
                            containerColor = Color.Transparent
                        )
                    )
                    if (index < state.monthlyBreakdown.lastIndex) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Recent Orders Header
            item {
                Text(
                    text = "Recent Orders",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // List of Orders
            if (state.recentOrders.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recent orders",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = state.recentOrders,
                    key = { _, order -> order.id }
                ) { index, order ->
                    RecentOrderListItem(
                        order = order,
                        onClick = { onAction(SellerDashboardAction.OnOrderClicked(order.id)) }
                    )
                    if (index < state.recentOrders.lastIndex) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}




@Composable
private fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    ElevatedCard(
        modifier = modifier,
        colors = CardDefaults.elevatedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RecentOrderListItem(
    order: RecentOrder,
    onClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Order Number & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Order #${order.orderNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatDate(order.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                StatusBadge(status = order.status)
            }

            // Overview details (Items summary & Amount)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val itemsSize = order.itemsCount
                val itemsText = if (itemsSize == 1) "1 item" else "$itemsSize items"
                Text(
                    text = itemsText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "KES ${"%,.2f".format(order.totalAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String?) {
    val cleanStatus = status?.uppercase() ?: "UNKNOWN"
    val containerColor = when (cleanStatus) {
        "PENDING" -> Color(0xFFFFF9C4) // yellow container
        "SHIPPED" -> Color(0xFFE3F2FD) // blue container
        "COMPLETED", "DELIVERED" -> Color(0xFFE8F5E9) // green container
        "CANCELLED" -> Color(0xFFFFEBEE) // red container
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when (cleanStatus) {
        "PENDING" -> Color(0xFFF57F17) // dark yellow text
        "SHIPPED" -> Color(0xFF1565C0) // dark blue text
        "COMPLETED", "DELIVERED" -> Color(0xFF2E7D32) // dark green text
        "CANCELLED" -> Color(0xFFC62828) // dark red text
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        contentColor = contentColor
    ) {
        Text(
            text = status ?: "UNKNOWN",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun formatDate(dateStr: String): String {
    return try {
        // Try parsing full ISO datetime
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val date = parser.parse(dateStr)
        val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
        if (date != null) formatter.format(date) else dateStr
    } catch (e: Exception) {
        try {
            val parser2 = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val date = parser2.parse(dateStr)
            val formatter = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
            if (date != null) formatter.format(date) else dateStr
        } catch (e2: Exception) {
            dateStr
        }
    }
}

@Composable
private fun MonthFilterDropdown(
    selectedFilter: String,
    monthlyBreakdown: List<MonthlyBreakdownDto>,
    onMonthSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val filterOptions = remember(monthlyBreakdown) {
        val list = mutableListOf("All Time", "This Month")
        monthlyBreakdown.forEach { breakdown ->
            val cleanMonth = formatMonthOnly(breakdown.monthLabel)
            if (cleanMonth != "Unknown" && !list.contains(cleanMonth)) {
                list.add(cleanMonth)
            }
        }
        list
    }

    Box {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = selectedFilter,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Select Month",
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            filterOptions.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onMonthSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatMonthOnly(monthLabel: String?): String {
    if (monthLabel.isNullOrBlank()) return "Unknown"
    val firstPart = monthLabel.split(" ").firstOrNull() ?: monthLabel
    return firstPart.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
}


