/**
 * ViewModel managing the business logic and UI state for the SellerDashboard feature.
 */
package com.agriflow.app.features.MyStore.sellerdashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.marketplace.MarketplaceRepository
import com.agriflow.app.features.orders.OrdersRepository
import com.agriflow.app.core.security.TokenRepository

import com.agriflow.app.features.auth.AuthRepository

@HiltViewModel
class SellerDashboardViewModel @Inject constructor(
    private val sellerDashboardRepository: SellerDashboardRepository,
    private val marketplaceRepository: MarketplaceRepository,
    private val ordersRepository: OrdersRepository,
    private val tokenRepository: TokenRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(SellerDashboardState())
    val state = _state.asStateFlow()

    private val _events = Channel<SellerDashboardEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadDashboardData()
        observeLocalProducts()
    }

    private fun observeLocalProducts() {
        viewModelScope.launch {
            val user = tokenRepository.getUserFlow().first()
            val username = user?.username.orEmpty()

            var businessId: String? = null
            var businessName: String? = null

            // Fetch business details to get the seller's business info
            when (val result = authRepository.getBusinessDetails()) {
                is Result.Success -> {
                    businessId = result.data.id
                    businessName = result.data.businessName
                }
                is Result.Error -> {
                    // Fallback to username only if fetch fails
                }
            }

            marketplaceRepository.observeProducts().collect { syncedProducts ->
                val sellerProducts = syncedProducts.filter { product ->
                    val matchesBusinessId = businessId != null && product.businessId == businessId
                    val matchesBusinessName = businessName != null && product.farmerName.equals(businessName, ignoreCase = true)
                    val matchesUsername = username.isNotBlank() && product.farmerName.equals(username, ignoreCase = true)

                    username.isBlank() || matchesBusinessId || matchesBusinessName || matchesUsername
                }
                _state.update { state ->
                    state.copy(
                        activeListings = sellerProducts.filter { it.availableQuantity > 0 }.size,
                        inventoryAlerts = sellerProducts.filter { it.availableQuantity <= 0 }.size
                    )
                }
            }
        }
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val analyticsJob = async { sellerDashboardRepository.getDashboardAnalytics() }
            val ordersJob = async { ordersRepository.getBusinessOrders() }

            val analyticsResult = analyticsJob.await()
            val ordersResult = ordersJob.await()

            var recentOrdersList = emptyList<RecentOrder>()
            var hasError = false

            when (analyticsResult) {
                is Result.Success -> {
                    val data = analyticsResult.data
                    _state.update { state ->
                        val displayRev = if (state.selectedMonthFilter == "All Time") {
                            "Ksh ${String.format("%.2f", data.totalRevenue ?: 0.0)}"
                        } else if (state.selectedMonthFilter == "This Month") {
                            "Ksh ${String.format("%.2f", data.thisMonthRevenue ?: 0.0)}"
                        } else {
                            val found = data.monthlyBreakdown?.find {
                                formatMonthOnly(it.monthLabel) == state.selectedMonthFilter
                            }
                            "Ksh ${String.format("%.2f", found?.revenue ?: 0.0)}"
                        }

                        val displayOrds = if (state.selectedMonthFilter == "All Time") {
                            data.totalOrders ?: 0
                        } else if (state.selectedMonthFilter == "This Month") {
                            data.thisMonthOrders ?: 0
                        } else {
                            val found = data.monthlyBreakdown?.find {
                                formatMonthOnly(it.monthLabel) == state.selectedMonthFilter
                            }
                            found?.orderCount ?: 0
                        }

                        state.copy(
                            totalRevenue = "Ksh ${String.format("%.2f", data.totalRevenue ?: 0.0)}",
                            thisMonthRevenue = "Ksh ${String.format("%.2f", data.thisMonthRevenue ?: 0.0)}",
                            totalOrders = data.totalOrders ?: 0,
                            thisMonthOrders = data.thisMonthOrders ?: 0,
                            pendingOrders = data.pendingOrders ?: 0,
                            deliveredOrders = data.deliveredOrders ?: 0,
                            revenueChangePercent = data.revenueChangePercent ?: 0.0,
                            monthlyBreakdown = data.monthlyBreakdown ?: emptyList(),
                            displayRevenue = displayRev,
                            displayOrders = displayOrds
                        )
                    }
                }
                is Result.Error -> {
                    hasError = true
                }
            }

            when (ordersResult) {
                is Result.Success -> {
                    val orders = ordersResult.data
                    recentOrdersList = orders
                        .sortedByDescending { it.createdAt.orEmpty() }
                        .take(10)
                        .map { dto ->
                            RecentOrder(
                                id = dto.id.orEmpty(),
                                orderNumber = dto.orderNumber.orEmpty(),
                                createdAt = dto.createdAt.orEmpty(),
                                status = dto.status.orEmpty(),
                                totalAmount = dto.totalAmount ?: 0.0,
                                itemsCount = dto.items?.size ?: 0
                            )
                        }
                }
                is Result.Error -> {
                    hasError = true
                }
            }

            _state.update {
                it.copy(
                    isLoading = false,
                    recentOrders = recentOrdersList,
                    errorMessage = if (hasError) "Failed to load dashboard data." else null
                )
            }
        }
    }

    private fun formatIsoDateTime(isoString: String?): String {
        if (isoString.isNullOrBlank()) return ""
        return try {
            val parts = isoString.split("T")
            if (parts.size == 2) {
                val date = parts[0]
                val time = parts[1].substringBefore(".").substringBefore("Z")
                "$date $time"
            } else {
                isoString
            }
        } catch (e: Exception) {
            isoString
        }
    }

    private fun formatMonthOnly(monthLabel: String?): String {
        if (monthLabel.isNullOrBlank()) return "Unknown"
        val firstPart = monthLabel.split(" ").firstOrNull() ?: monthLabel
        return firstPart.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
    }

    fun onAction(action: SellerDashboardAction) {
        when (action) {
            SellerDashboardAction.OnExportReportClicked -> {
                viewModelScope.launch {
                    _events.send(SellerDashboardEvent.ShowSnackbarMessage("Exporting dashboard report..."))
                }
            }
            is SellerDashboardAction.OnOrderClicked -> {
                viewModelScope.launch {
                    _events.send(SellerDashboardEvent.NavigateToOrderDetail(action.orderId))
                }
            }
            SellerDashboardAction.OnAddProductClicked -> {
                viewModelScope.launch {
                    _events.send(SellerDashboardEvent.NavigateToAddProduct)
                }
            }
            is SellerDashboardAction.OnMonthFilterSelected -> {
                _state.update { state ->
                    val displayRev = if (action.monthLabel == "All Time") {
                        state.totalRevenue
                    } else if (action.monthLabel == "This Month") {
                        state.thisMonthRevenue
                    } else {
                        val found = state.monthlyBreakdown.find {
                            formatMonthOnly(it.monthLabel) == action.monthLabel
                        }
                        "Ksh ${String.format("%.2f", found?.revenue ?: 0.0)}"
                    }

                    val displayOrds = if (action.monthLabel == "All Time") {
                        state.totalOrders
                    } else if (action.monthLabel == "This Month") {
                        state.thisMonthOrders
                    } else {
                        val found = state.monthlyBreakdown.find {
                            formatMonthOnly(it.monthLabel) == action.monthLabel
                        }
                        found?.orderCount ?: 0
                    }

                    state.copy(
                        selectedMonthFilter = action.monthLabel,
                        displayRevenue = displayRev,
                        displayOrders = displayOrds
                    )
                }
            }
        }
    }
}


