package com.agriflow.app.features.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.auth.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val ordersRepository: OrdersRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OrdersState())
    val state = _state.asStateFlow()

    private val _events = Channel<OrdersEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            tokenRepository.getUserRoleFlow().collect { role ->
                _state.update { it.copy(activeRole = role) }
                loadOrdersForActiveRole()
            }
        }
    }

    fun onAction(action: OrdersAction) {
        when (action) {
            OrdersAction.RefreshOrders -> {
                loadOrdersForActiveRole()
            }
            is OrdersAction.ToggleOrderDetails -> {
                _state.update {
                    val newExpanded = if (it.expandedOrderId == action.orderId) null else action.orderId
                    it.copy(expandedOrderId = newExpanded)
                }
            }
            is OrdersAction.UpdateShipmentStatus -> {
                updateOrderStatus(action.orderId, action.status, action.trackingNumber, action.carrier)
            }
        }
    }

    private fun loadOrdersForActiveRole() {
        val currentRole = _state.value.activeRole
        if (currentRole == UserRole.FARMER || currentRole == UserRole.SUPPLIER) {
            loadBusinessOrders()
        } else {
            loadBuyerOrders()
        }
    }

    private fun loadBuyerOrders() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = ordersRepository.getMyOrders()) {
                is Result.Success -> {
                    val activeOrders = result.data.filter { order ->
                        val status = order.status?.uppercase() ?: ""
                        status != "PENDING_PAYMENT" && status != "UNPAID"
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            orders = activeOrders.sortedByDescending { order -> order.createdAt.orEmpty() }
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load orders: ${result.error.name}"
                        )
                    }
                    _events.send(OrdersEvent.ShowSnackbar("Error loading orders: ${result.error.name}"))
                }
            }
        }
    }

    private fun loadBusinessOrders() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = ordersRepository.getBusinessOrders()) {
                is Result.Success -> {
                    val activeOrders = result.data.filter { order ->
                        val status = order.status?.uppercase() ?: ""
                        status != "PENDING_PAYMENT" && status != "UNPAID"
                    }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            orders = activeOrders.sortedByDescending { order -> order.createdAt.orEmpty() }
                        )
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Failed to load store orders: ${result.error.name}"
                        )
                    }
                    _events.send(OrdersEvent.ShowSnackbar("Error loading store orders: ${result.error.name}"))
                }
            }
        }
    }

    private fun updateOrderStatus(
        orderId: String,
        status: String,
        trackingNumber: String?,
        carrier: String?
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            when (val result = ordersRepository.updateOrderStatus(orderId, status, trackingNumber, carrier)) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(OrdersEvent.ShowSnackbar("Shipment status updated successfully!"))
                    loadOrdersForActiveRole()
                }
                is Result.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(OrdersEvent.ShowSnackbar("Failed to update status: ${result.error.name}"))
                }
            }
        }
    }
}
