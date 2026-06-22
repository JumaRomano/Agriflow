/**
 * ViewModel managing the business logic and UI state for the MyProducts feature.
 */
package com.agriflow.app.features.MyStore.myproducts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.marketplace.MarketplaceRepository
import com.agriflow.app.core.security.TokenRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.combine

@HiltViewModel
class MyProductsViewModel @Inject constructor(
    private val repository: MarketplaceRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MyProductsState())
    val state = _state.asStateFlow()

    private val _events = Channel<MyProductsEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeStoreProducts()
        loadListedProducts()
    }

    private fun observeStoreProducts() {
        viewModelScope.launch {
            val user = tokenRepository.getUserFlow().first()
            val username = user?.username.orEmpty()

            combine(
                repository.observeProducts(),
                repository.observeStoreInventory()
            ) { syncedProducts, offlineDrafts ->
                // Filter synced products where farmerName matches the seller's username
                val syncedListed = syncedProducts
                    .filter { product ->
                        username.isBlank() || product.farmerName.equals(username, ignoreCase = true)
                    }
                    .map { product ->
                        ListedProduct(
                            id = product.id,
                            name = product.name,
                            price = product.priceCents / 100.0,
                            stockQuantity = product.availableQuantity.toInt(),
                            unit = product.quantityUnit,
                            imageUrl = product.imageUrl.orEmpty(),
                            status = if (product.availableQuantity <= 0) ProductStatus.OUT_OF_STOCK else ProductStatus.ACTIVE
                        )
                    }

                val draftListed = offlineDrafts.map { draft ->
                    ListedProduct(
                        id = draft.id,
                        name = draft.productName,
                        price = draft.price,
                        stockQuantity = draft.quantity,
                        unit = draft.unit,
                        imageUrl = if (draft.imageUrls.isNotEmpty()) draft.imageUrls.split(",").first() else "",
                        status = ProductStatus.UNDER_REVIEW
                    )
                }

                syncedListed + draftListed
            }.collect { combinedList ->
                _state.update { it.copy(products = combinedList) }
            }
        }
    }

    private fun loadListedProducts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            // Trigger sync of offline drafts in the background first
            repository.syncPendingInventory()

            when (val result = repository.getMyProducts()) {
                is Result.Success -> {
                    _state.update { it.copy(isLoading = false) }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = if (_state.value.products.isEmpty()) "Could not fetch your products." else null
                        )
                    }
                    _events.send(MyProductsEvent.ShowSnackbarMessage("Failed to fetch products from backend."))
                }
            }
        }
    }

    fun onAction(action: MyProductsAction) {
        when (action) {
            MyProductsAction.OnAddProductClicked -> {
                viewModelScope.launch {
                    _events.send(MyProductsEvent.MapsToAddProduct(null))
                }
            }
            is MyProductsAction.OnProductClicked -> {
                viewModelScope.launch {
                    _events.send(MyProductsEvent.MapsToProductDetails(action.productId))
                }
            }
            is MyProductsAction.OnEditClicked -> {
                viewModelScope.launch {
                    _events.send(MyProductsEvent.MapsToAddProduct(action.productId))
                }
            }
            is MyProductsAction.OnDeleteClicked -> {
                // Mock deleting product
                val deletedId = action.productId
                _state.update { state ->
                    val filtered = state.products.filterNot { it.id == deletedId }
                    state.copy(products = filtered)
                }
                viewModelScope.launch {
                    _events.send(MyProductsEvent.ShowSnackbarMessage("Product deleted successfully"))
                }
            }
        }
    }
}
