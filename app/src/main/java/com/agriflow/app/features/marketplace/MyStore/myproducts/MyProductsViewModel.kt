/**
 * ViewModel managing the business logic and UI state for the MyProducts feature.
 */
package com.agriflow.app.features.marketplace.MyStore.myproducts

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

@HiltViewModel
class MyProductsViewModel @Inject constructor(
    private val repository: MarketplaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MyProductsState())
    val state = _state.asStateFlow()

    private val _events = Channel<MyProductsEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadListedProducts()
    }

    private fun loadListedProducts() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = repository.getMyProducts()) {
                is Result.Success -> {
                    val uiProducts = result.data.map { product ->
                        ListedProduct(
                            id = product.id,
                            name = product.name,
                            price = product.priceCents / 100.0,
                            stockQuantity = product.availableQuantity.toInt(),
                            unit = product.quantityUnit,
                            imageUrl = product.imageUrl.orEmpty(),
                            status = ProductStatus.ACTIVE // Or custom logic to determine status
                        )
                    }
                    _state.update {
                        it.copy(products = uiProducts, isLoading = false)
                    }
                }
                is Result.Error -> {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Could not fetch your products."
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
