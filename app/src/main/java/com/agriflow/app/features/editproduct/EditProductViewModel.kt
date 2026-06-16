/**
 * ViewModel managing the business logic and UI state for the EditProduct feature.
 * Coordinates product details loading, validation, saving via PATCH, and stock alert out-of-stock actions.
 */
package com.agriflow.app.features.editproduct

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.agriflow.app.core.navigation.Route
import com.agriflow.app.core.util.FileHelper
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.marketplace.MarketplaceRepository
import com.agriflow.app.features.marketplace.ProductUpdateRequest
import com.agriflow.app.features.marketplace.ProductUploadRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProductViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val marketplaceRepository: MarketplaceRepository,
    private val fileHelper: FileHelper
) : ViewModel() {

    private val _state = MutableStateFlow(EditProductState())
    val state = _state.asStateFlow()

    private val _events = Channel<EditProductEvent>()
    val events = _events.receiveAsFlow()

    private val productId: String
    private var isInitialized = false

    init {
        fetchCategories()
        val routeArgs = savedStateHandle.toRoute<Route.EditProduct>()
        productId = routeArgs.productId
        
        viewModelScope.launch {
            marketplaceRepository.getProductById(productId).collect { product ->
                if (product != null && !isInitialized) {
                    isInitialized = true
                    _state.update {
                        it.copy(
                            productId = productId,
                            name = product.name,
                            description = product.description,
                            price = (product.priceCents / 100.0).toString(),
                            quantity = product.availableQuantity.toInt().toString(),
                            selectedCategoryId = product.category,
                            selectedUnit = product.quantityUnit,
                            selectedImageUris = if (!product.imageUrl.isNullOrBlank()) {
                                listOf(Uri.parse(product.imageUrl))
                            } else emptyList()
                        )
                    }
                }
            }
        }
    }

    fun onAction(action: EditProductAction) {
        when (action) {
            is EditProductAction.OnNameChanged -> {
                _state.update { it.copy(name = action.name, nameError = null) }
            }
            is EditProductAction.OnDescriptionChanged -> {
                _state.update { it.copy(description = action.description, descriptionError = null) }
            }
            is EditProductAction.OnPriceChanged -> {
                _state.update { it.copy(price = action.price, priceError = null) }
            }
            is EditProductAction.OnQuantityChanged -> {
                _state.update { it.copy(quantity = action.quantity, quantityError = null) }
            }
            is EditProductAction.OnCategorySelected -> {
                _state.update { it.copy(selectedCategoryId = action.categoryId, categoryError = null) }
            }
            is EditProductAction.OnUnitSelected -> {
                _state.update { it.copy(selectedUnit = action.unit, unitError = null) }
            }
            is EditProductAction.OnImagesSelected -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    val (uploadedUrls, uploadFailed) = uploadLocalImages(action.uris)
                    _state.update {
                        val currentList = it.selectedImageUris
                        val updatedList = (currentList + uploadedUrls).distinct().take(5)
                        it.copy(
                            selectedImageUris = updatedList,
                            isLoading = false,
                            imagesError = if (uploadFailed) "Some images failed to upload" else null
                        )
                    }
                    if (uploadFailed) {
                        _events.send(EditProductEvent.ShowSnackbar("Failed to upload some images"))
                    }
                }
            }
            is EditProductAction.OnRemoveImage -> {
                _state.update {
                    val updatedList = it.selectedImageUris.filterNot { uri -> uri == action.uri }
                    it.copy(selectedImageUris = updatedList)
                }
            }
            EditProductAction.OnSaveClicked -> {
                saveProductChanges()
            }
            EditProductAction.OnMarkOutOfStockClicked -> {
                markProductAsOutOfStock()
            }
        }
    }

    private suspend fun uploadLocalImages(uris: List<Uri>): Pair<List<Uri>, Boolean> {
        val urls = mutableListOf<Uri>()
        var failed = false
        for (uri in uris) {
            if (uri.scheme == "http" || uri.scheme == "https") {
                urls.add(uri)
                continue
            }
            val tempFile = fileHelper.uriToFile(uri)
            if (tempFile == null) {
                failed = true
                continue
            }
            val part = fileHelper.fileToMultipartPart(tempFile)
            val uploadResult = marketplaceRepository.uploadImage(part)
            tempFile.delete()
            
            when (uploadResult) {
                is Result.Success -> {
                    val imageUrl = uploadResult.data.url
                    if (!imageUrl.isNullOrBlank()) {
                        urls.add(Uri.parse(imageUrl))
                    } else {
                        failed = true
                    }
                }
                is Result.Error -> {
                    failed = true
                }
            }
        }
        return Pair(urls, failed)
    }

    private fun saveProductChanges() {
        val currentState = state.value
        val isNameValid = currentState.name.isNotBlank()
        val isDescriptionValid = currentState.description.isNotBlank()
        
        val parsedPrice = currentState.price.toDoubleOrNull()
        val isPriceValid = parsedPrice != null && parsedPrice > 0.0
        
        val parsedQuantity = currentState.quantity.toIntOrNull()
        val isQuantityValid = parsedQuantity != null && parsedQuantity >= 0
        
        val isCategoryValid = !currentState.selectedCategoryId.isNullOrBlank()
        val isUnitValid = currentState.selectedUnit.isNotBlank()
        val isImagesValid = currentState.selectedImageUris.isNotEmpty()

        if (!isNameValid || !isDescriptionValid || !isPriceValid || !isQuantityValid || 
            !isCategoryValid || !isUnitValid || !isImagesValid) {
            
            _state.update {
                it.copy(
                    nameError = if (isNameValid) null else "Product name is required",
                    descriptionError = if (isDescriptionValid) null else "Description is required",
                    priceError = when {
                        currentState.price.isBlank() -> "Price is required"
                        parsedPrice == null -> "Price must be a valid number"
                        parsedPrice <= 0.0 -> "Price must be greater than zero"
                        else -> null
                    },
                    quantityError = when {
                        currentState.quantity.isBlank() -> "Quantity is required"
                        parsedQuantity == null -> "Quantity must be a valid integer"
                        parsedQuantity < 0 -> "Quantity cannot be negative"
                        else -> null
                    },
                    categoryError = if (isCategoryValid) null else "Please select a category",
                    unitError = if (isUnitValid) null else "Please select a unit",
                    imagesError = if (isImagesValid) null else "At least one product image is required"
                )
            }
            viewModelScope.launch {
                _events.send(EditProductEvent.ShowSnackbar("Please fix the validation errors in the form."))
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val request = ProductUpdateRequest(
                productName = currentState.name,
                description = currentState.description,
                price = parsedPrice ?: 0.0,
                quantity = parsedQuantity ?: 0,
                unit = currentState.selectedUnit,
                categoryId = currentState.selectedCategoryId ?: ""
            )

            val apiResult = marketplaceRepository.updateProduct(productId, request)
            _state.update { it.copy(isLoading = false) }

            when (apiResult) {
                is Result.Success -> {
                    _state.update { it.copy(success = true) }
                    _events.send(EditProductEvent.ShowSnackbar("Product updated successfully!"))
                    _events.send(EditProductEvent.SaveSuccess)
                }
                is Result.Error -> {
                    _events.send(EditProductEvent.ShowSnackbar("Failed to update product: ${apiResult.error}"))
                }
            }
        }
    }

    private fun markProductAsOutOfStock() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val apiResult = marketplaceRepository.markAsOutOfStock(productId)
            _state.update { it.copy(isLoading = false) }

            when (apiResult) {
                is Result.Success -> {
                    _state.update { it.copy(success = true, quantity = "0") }
                    _events.send(EditProductEvent.ShowSnackbar("Product marked as Out of Stock successfully!"))
                    _events.send(EditProductEvent.SaveSuccess)
                }
                is Result.Error -> {
                    _events.send(EditProductEvent.ShowSnackbar("Failed to alert out-of-stock: ${apiResult.error}"))
                }
            }
        }
    }

    private fun fetchCategories() {
        viewModelScope.launch {
            _state.update { it.copy(isFetchingCategories = true) }
            when (val result = marketplaceRepository.getCategories()) {
                is Result.Success -> {
                    _state.update {
                        it.copy(
                            categories = result.data,
                            isFetchingCategories = false
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { it.copy(isFetchingCategories = false) }
                    _events.send(
                        EditProductEvent.ShowSnackbar("Failed to fetch categories: ${result.error}")
                    )
                }
            }
        }
    }
}
