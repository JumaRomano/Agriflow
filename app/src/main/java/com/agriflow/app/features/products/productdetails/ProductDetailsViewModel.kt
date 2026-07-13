/**
 * ViewModel managing the business logic and UI state for the ProductDetails feature.
 */
package com.agriflow.app.features.products.productdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.agriflow.app.core.navigation.Route
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.cart.domain.CartRepository
import com.agriflow.app.features.marketplace.MarketplaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductDetailsViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val marketplaceRepository: MarketplaceRepository,
    private val tokenRepository: TokenRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val routeArgs = savedStateHandle.toRoute<Route.ProductDetails>()
    private val productId = routeArgs.productId

    private val _selectedQuantity = MutableStateFlow(1)
    private val _supplierLogoUrl = MutableStateFlow<String?>(null)
    private val _supplierRating = MutableStateFlow<Double?>(null)
    private val _events = Channel<ProductDetailsEvent>()
    val events = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            var lastFetchedBusinessId: String? = null
            marketplaceRepository.getProductById(productId).collect { product ->
                val businessId = product?.businessId
                if (!businessId.isNullOrEmpty() && businessId != lastFetchedBusinessId) {
                    lastFetchedBusinessId = businessId
                    when (val result = marketplaceRepository.getBusinessPublicDetails(businessId)) {
                        is Result.Success -> {
                            _supplierLogoUrl.value = result.data.businessProfile
                            _supplierRating.value = result.data.rating
                        }
                        is Result.Error -> {
                            // Fallback/ignore if API fails
                        }
                    }
                }
            }
        }
    }

    val state = combine(
        marketplaceRepository.getProductById(productId),
        _selectedQuantity,
        tokenRepository.getUserRoleFlow(),
        _supplierLogoUrl,
        _supplierRating
    ) { product, quantity, role, logoUrl, rating ->
        ProductDetailsState(
            product = product,
            selectedQuantity = quantity,
            isLoading = product == null,
            userRole = role,
            supplierLogoUrl = logoUrl,
            supplierRating = rating
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ProductDetailsState()
    )

    fun onAction(action: ProductDetailsAction) {
        when (action) {
            ProductDetailsAction.OnIncrementQuantity -> {
                val available = state.value.product?.availableQuantity?.toInt() ?: 1
                if (_selectedQuantity.value < available) {
                    _selectedQuantity.value += 1
                }
            }
            ProductDetailsAction.OnDecrementQuantity -> {
                if (_selectedQuantity.value > 1) {
                    _selectedQuantity.value -= 1
                }
            }
            ProductDetailsAction.OnAddToCart -> {
                viewModelScope.launch {
                    val product = state.value.product
                    if (product != null) {
                        if (product.availableQuantity <= 0) {
                            _events.send(ProductDetailsEvent.ShowSnackbar("This item is out of stock."))
                            return@launch
                        }
                        _events.send(ProductDetailsEvent.ShowSnackbar("Adding ${product.name} to cart..."))
                        when (val result = cartRepository.addToCart(product.id, _selectedQuantity.value.toDouble())) {
                            is Result.Success -> {
                                _events.send(ProductDetailsEvent.ShowSnackbar("${_selectedQuantity.value} ${product.quantityUnit} of ${product.name} added to cart!"))
                            }
                            is Result.Error -> {
                                _events.send(ProductDetailsEvent.ShowSnackbar("Failed to add to cart: ${result.error.name}"))
                            }
                        }
                    }
                }
            }
            ProductDetailsAction.OnNavigateBack -> {
                viewModelScope.launch {
                    _events.send(ProductDetailsEvent.MapsBack)
                }
            }
            is ProductDetailsAction.OnSupplierClick -> {
                viewModelScope.launch {
                    if (action.businessId.isNotEmpty()) {
                        _events.send(ProductDetailsEvent.NavigateToBusinessDetails(action.businessId))
                    }
                }
            }
        }
    }
}
