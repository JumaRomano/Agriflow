/**
 * ViewModel managing the business logic and UI state for the Cart feature.
 */
package com.agriflow.app.features.cart.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.cart.domain.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CartState())
    val state = _state.asStateFlow()

    private val _events = Channel<String>()
    val events = _events.receiveAsFlow()

    // Map to track active debounce jobs for each product's quantity updates
    private val updateJobs = mutableMapOf<String, Job>()

    // Map to track accumulated deltas for each product's quantity updates
    private val accumulatedDeltas = mutableMapOf<String, Double>()

    init {
        onAction(CartAction.FetchCart)
    }

    fun onAction(action: CartAction) {
        when (action) {
            CartAction.FetchCart -> fetchCart()
            is CartAction.UpdateQuantity -> updateQuantity(action.productId, action.itemId, action.newQuantity)
            is CartAction.RemoveItem -> removeItem(action.itemId)
            CartAction.ClearCart -> clearCart()
            CartAction.Checkout -> checkout()
        }
    }

    private fun fetchCart() {
        viewModelScope.launch {
            _state.update { it.copy(isFetchingCart = true, errorMessage = null) }
            when (val result = cartRepository.getCart()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            items = result.data.items,
                            subtotal = result.data.subtotal,
                            shippingFee = result.data.shippingFee,
                            total = result.data.total,
                            isFetchingCart = false
                        )
                    }
                }
                is Result.Error -> {
                    // differentiates error and not found
                    if (result.error.name == "NOT_FOUND" || result.error.name == "EMPTY_CART") {
                        _state.update {
                            it.copy(
                                items = emptyList(),
                                subtotal = 0.0,
                                shippingFee = 0.0,
                                total = 0.0,
                                isFetchingCart = false,
                                errorMessage = null
                            )
                        }
                    } else {
                        _state.update {
                            it.copy(
                                isFetchingCart = false,
                                errorMessage = "Failed to load cart items."
                            )
                        }
                        _events.send("Failed to sync cart: ${result.error.name}")
                    }
                }
            }
        }
    }

    private fun updateQuantity(productId: String, itemId: String, quantityDelta: Double) {
        // Cancel any pending update job for this specific product to debounce rapid taps
        updateJobs[productId]?.cancel()

        // Accumulate quantity delta for the debounced network sync
        val currentDelta = accumulatedDeltas[productId] ?: 0.0
        val newDelta = currentDelta + quantityDelta
        accumulatedDeltas[productId] = newDelta

        // 1. Calculate the new absolute quantity for the optimistic UI update
        val currentItems = _state.value.items
        val targetItem = currentItems.find { it.id == itemId } ?: return
        val newAbsoluteQuantity = targetItem.quantity + quantityDelta

        // 2. Optimistically update the UI so it feels instantly responsive
        _state.update { state ->
            val updatedList = state.items.map { item ->
                // Update the UI with the absolute total for the specific item
                if (item.id == itemId) item.copy(quantity = newAbsoluteQuantity) else item
            }
            state.copy(items = updatedList)
        }

        // Launch a debounced background sync job
        val job = viewModelScope.launch {
            // Wait for user to stop tapping
            delay(400)

            // Mark this specific item as updating (row-level loading)
            _state.update { it.copy(updatingItemIds = it.updatingItemIds + itemId) }

            // Retrieve the cumulative delta to send to the repository, then clear it
            val deltaToSend = accumulatedDeltas[productId] ?: quantityDelta
            accumulatedDeltas.remove(productId)

            val result = if (deltaToSend < 0.0) {
                val remainingQuantity = _state.value.items.find { it.id == itemId }?.quantity ?: 0.0
                cartRepository.deductCartItem(itemId, remainingQuantity)
            } else {
                cartRepository.addToCart(productId, deltaToSend)
            }

            when (result) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            items = result.data.items,
                            subtotal = result.data.subtotal,
                            shippingFee = result.data.shippingFee,
                            total = result.data.total
                        )
                    }
                }
                is Result.Error -> {
                    _events.send("Failed to update item: ${result.error.name}")
                    // Refresh cart to revert UI to server state in case of failure
                    fetchCart()
                }
            }


            _state.update { it.copy(updatingItemIds = it.updatingItemIds - itemId) }
        }

        updateJobs[productId] = job
    }
    private fun removeItem(itemId: String) {
        viewModelScope.launch {
            // Mark item as loading
            _state.update { it.copy(updatingItemIds = it.updatingItemIds + itemId) }

            when (val result = cartRepository.removeCartItem(itemId)) {
                is Result.Success -> {
                    _events.send("Item removed from cart.")
                    fetchCart()
                }
                is Result.Error -> {
                    _events.send("Failed to remove item: ${result.error.name}")
                }
            }

            _state.update { it.copy(updatingItemIds = it.updatingItemIds - itemId) }
        }
    }

    private fun clearCart() {
        viewModelScope.launch {
            _state.update { it.copy(isClearingCart = true) }
            when (val result = cartRepository.clearCart()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            items = emptyList(),
                            subtotal = 0.0,
                            shippingFee = 0.0,
                            total = 0.0,
                            isClearingCart = false
                        )
                    }
                    _events.send("Cart cleared successfully.")
                }
                is Result.Error -> {
                    _state.update { it.copy(isClearingCart = false) }
                    _events.send("Failed to clear cart: ${result.error.name}")
                }
            }
        }
    }

    private fun checkout() {
        viewModelScope.launch {
            _events.send("NavigateToCheckout")
        }
    }
}
