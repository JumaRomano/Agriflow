package com.agriflow.app.features.ratings.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.agriflow.app.features.orders.OrderDto
import com.agriflow.app.features.ratings.ui.RatingState
import com.agriflow.app.features.ratings.ui.RatingsViewModel

sealed interface RateableItem {
    val id: String
    val name: String

    data class Business(override val id: String, override val name: String) : RateableItem
    data class Product(
        override val id: String,
        override val name: String,
        val imageUrl: String?
    ) : RateableItem
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderRatingHub(
    order: OrderDto,
    viewModel: RatingsViewModel,
    onDismissRequest: () -> Unit
) {
    val itemStates by viewModel.itemStates.collectAsState()
    var itemToRate by remember { mutableStateOf<RateableItem?>(null) }

    LaunchedEffect(order.id) {
        viewModel.loadRatingsForOrder(order)
    }

    // Dismiss dialog automatically when state changes to Rated
    LaunchedEffect(itemToRate, itemStates) {
        val currentItem = itemToRate
        if (currentItem != null) {
            val state = itemStates[currentItem.id]
            if (state is RatingState.Rated) {
                itemToRate = null
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Rate Order Items",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Order #${order.orderNumber ?: ""}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismissRequest,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Parse eligible items
            val items = order.items.orEmpty()
            val businesses = remember(order) {
                val list = items.mapNotNull { item ->
                    val id = item.businessId
                    if (id != null) {
                        val name = item.businessName ?: "Seller"
                        RateableItem.Business(id, name)
                    } else null
                }.distinctBy { it.id }.toMutableList()

                order.businessId?.let { bId ->
                    if (list.none { it.id == bId }) {
                        list.add(RateableItem.Business(bId, "Seller"))
                    }
                }
                list.toList()
            }

            val products = remember(order) {
                items.mapNotNull { item ->
                    val id = item.productId
                    if (id != null) {
                        val name = item.productName ?: "Product"
                        RateableItem.Product(id, name, item.productImage)
                    } else null
                }.distinctBy { it.id }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                if (businesses.isNotEmpty()) {
                    item {
                        Text(
                            text = "Businesses",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(businesses) { business ->
                        val ratingState = itemStates[business.id] ?: RatingState.Loading
                        RateableItemRow(
                            item = business,
                            state = ratingState,
                            onRateClick = { itemToRate = business },
                            onDeleteClick = { viewModel.deleteBusinessRatingNew(business.id) }
                        )
                    }
                }

                if (products.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Products",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(products) { product ->
                        val ratingState = itemStates[product.id] ?: RatingState.Loading
                        RateableItemRow(
                            item = product,
                            state = ratingState,
                            onRateClick = { itemToRate = product },
                            onDeleteClick = { viewModel.deleteProductRating(product.id) }
                        )
                    }
                }
            }
        }
    }

    if (itemToRate != null) {
        val currentItem = itemToRate!!
        val ratingState = itemStates[currentItem.id]
        SubmitRatingDialog(
            itemName = currentItem.name,
            state = ratingState,
            onDismissRequest = { itemToRate = null },
            onSubmit = { ratingVal, comment ->
                when (currentItem) {
                    is RateableItem.Business -> {
                        viewModel.submitBusinessRating(currentItem.id, ratingVal, comment ?: "")
                    }
                    is RateableItem.Product -> {
                        viewModel.submitProductRating(currentItem.id, ratingVal, comment ?: "")
                    }
                }
            }
        )
    }
}

@Composable
fun RateableItemRow(
    item: RateableItem,
    state: RatingState,
    onRateClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Visual representation
            when (item) {
                is RateableItem.Business -> {
                    // Circular icon placeholder for business
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.name.take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                is RateableItem.Product -> {
                    // Rounded square product image
                    if (!item.imageUrl.isNullOrEmpty()) {
                        AsyncImage(
                            model = item.imageUrl,
                            contentDescription = item.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "📦",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            // Name and status details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                when (state) {
                    is RatingState.Loading -> {
                        Box(modifier = Modifier.height(16.dp)) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                strokeWidth = 1.5.dp
                            )
                        }
                    }
                    is RatingState.Unrated -> {
                        if (state.error != null) {
                            Text(
                                text = state.error,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = "Not rated yet",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                    is RatingState.Rated -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                for (i in 1..5) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = null,
                                        tint = if (i <= state.ratingValue) Color(0xFFFFB300) else Color.LightGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "• Rated",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (!state.comment.isNullOrBlank()) {
                            Text(
                                text = "\"${state.comment}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }

            // Action button
            when (state) {
                is RatingState.Loading -> {
                    // No action while loading
                }
                is RatingState.Unrated -> {
                    Button(
                        onClick = onRateClick,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text(
                            text = if (state.error != null) "Retry" else "Rate",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                is RatingState.Rated -> {
                    IconButton(
                        onClick = onDeleteClick,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Rating"
                        )
                    }
                }
            }
        }
    }
}
