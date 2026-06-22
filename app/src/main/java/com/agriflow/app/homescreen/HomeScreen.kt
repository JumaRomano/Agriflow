/**
 * Jetpack Compose UI screen components for the Home screen.
 */
package com.agriflow.app.homescreen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import com.agriflow.app.features.auth.UserRole
import com.agriflow.app.features.marketplace.ProductGridItem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.compose.runtime.remember as remember


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeRoute(
    onNavigateToMarketplace: (String?) -> Unit,
    onNavigateToSupplierNetwork: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToWallet: () -> Unit,
    onNavigateToProductDetails: (String) -> Unit,
    onNavigateToBusinessDetails: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.NavigateToMarketplace -> onNavigateToMarketplace(null)
                is HomeEvent.NavigateToBusinessDetails -> onNavigateToBusinessDetails(event.businessId)
                HomeEvent.NavigateToSupplierNetwork -> onNavigateToSupplierNetwork()
                HomeEvent.NavigateToCart -> onNavigateToCart()
                HomeEvent.NavigateToNotification -> onNavigateToNotification()
                HomeEvent.NavigateToWallet -> onNavigateToWallet()
                is HomeEvent.NavigateToProductDetails -> onNavigateToProductDetails(event.productId)
                is HomeEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    HomeScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onAction = viewModel::onAction
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeState,

    snackbarHostState: SnackbarHostState,
    onAction: (HomeAction) -> Unit
) {
    val homeProducts = remember(state.selectedCategory, state.products) {
        if (state.selectedCategory.equals("All", ignoreCase = true)) {
            state.products
        } else {
            state.products.filter {
                it.category.equals(state.selectedCategory, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agriflow ", fontWeight = FontWeight.Bold) },
                actions = {
                    if (state.userRole == UserRole.BUYER) {
                        IconButton(onClick = { onAction(HomeAction.CartClicked) }) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Cart")
                        }
                    }
                    IconButton(onClick = { onAction(HomeAction.NotificationsClicked) }) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                    if (state.userRole == UserRole.SUPPLIER || state.userRole == UserRole.FARMER )
                    IconButton(onClick = { onAction(HomeAction.WalletClicked) }) {
                        Icon(imageVector =  Icons.Default.AccountBalanceWallet, contentDescription = "Wallet")
                    }

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Hero section
            item {
                Text(
                    text = "Welcome "+ state.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Hero section
            item {
                Text(
                    text = "Connect directly with verified enterprise distributors, make your orders, and optimize your supply chain from seed to sale.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {

                    var isDropdownExpanded by remember { mutableStateOf(false) }

                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = {
                            onAction(HomeAction.SearchQueryChanged(it))
                            isDropdownExpanded = true
                        },
                        placeholder = { Text("Search Products") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    val filteredProducts = remember(state.searchQuery, state.products) {
                        if (state.searchQuery.isBlank()) {
                            emptyList()
                        } else {
                            state.products.filter {
                                it.name.contains(state.searchQuery, ignoreCase = true)
                            }
                        }
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded && filteredProducts.isNotEmpty(),
                        onDismissRequest = { isDropdownExpanded = false },
                        properties = PopupProperties(focusable = false),
                        modifier = Modifier.fillMaxWidth(0.9f)
                    ) {
                        filteredProducts.take(6).forEach { product ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        if (product.imageUrl != null) {
                                            AsyncImage(
                                                model = product.imageUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("📦", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                        Column {
                                            Text(
                                                text = product.name,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${product.currencyCode} ${product.priceCents / 100.0} - By ${product.farmerName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    onAction(HomeAction.ProductClicked(product))
                                    onAction(HomeAction.SearchQueryChanged(""))
                                    isDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Categories Section
            if (state.categories.isNotEmpty()) {
                item {
                    CategoryRow(
                        categories = state.categories,
                        onCategorySelected = { categoryName ->
                            onAction(HomeAction.CategorySelected(categoryName))
                        }
                    )
                }
            }

            // Verified Distributors Section
            if (state.distributors.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Verified Farms & Suppliers",
                        onSeeAllClick = { onAction(HomeAction.ViewSupplierNetworkClicked) }
                    )
                }
                item {
                    DistributorRow(
                        distributors = state.distributors,
                        onDistributorClick = { distributor ->
                            onAction(HomeAction.DistributorClicked(distributor.id))
                        }
                    )
                }
            }

            // Featured Products Section
            item {
                SectionHeader(
                    title = if (state.selectedCategory == "All") "Featured Products" else "Products in ${state.selectedCategory}",
                    onSeeAllClick = { onAction(HomeAction.StartSourcingClicked) }
                )
            }

            if (homeProducts.isNotEmpty()) {
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(homeProducts.take(10)) { product ->
                            ProductGridItem(
                                product = product,
                                onClick = { onAction(HomeAction.ProductClicked(product)) },
                                modifier = Modifier.width(160.dp)
                            )
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📦", style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No products found in ${state.selectedCategory}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier,
    actionText: String = "See all"
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(
            onClick = onSeeAllClick,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "→",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun CategoryRow(
    categories: List<HomeCategory>,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(categories) { category ->
            val isSelected = category.isSelected
            val containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            }
            val contentColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
            val borderColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                Color.Transparent
            }

            Surface(
                onClick = { onCategorySelected(category.name) },
                shape = RoundedCornerShape(12.dp),
                color = containerColor,
                contentColor = contentColor,
                border = if (isSelected) BorderStroke(1.5.dp, borderColor) else null,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .animateContentSize()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun DistributorRow(
    distributors: List<Distributor>,
    onDistributorClick: (Distributor) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 4.dp)
    ) {
        items(distributors) { distributor ->
            Card(
                onClick = { onDistributorClick(distributor) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .width(220.dp)
                    .height(130.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Verification Badge
                        if (distributor.isVerified) {
                            Row(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        RoundedCornerShape(100.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified Business",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Verified",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Column {
                        Text(
                            text = distributor.brandName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = distributor.tagline,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Star"
                        )

                        Text(
                            text = "${distributor.rating}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "(${distributor.reviewCount})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
