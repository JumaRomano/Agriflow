/**
 * ViewModel managing the business logic and UI state for the AddProduct feature.
 */
package com.agriflow.app.features.addproduct

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.agriflow.app.core.navigation.Route
import com.agriflow.app.core.util.FileHelper
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.marketplace.MarketplaceRepository
import com.agriflow.app.features.marketplace.ProductUploadRequest
import com.agriflow.app.features.marketplace.MyStore.myproducts.ListedProduct
import com.agriflow.app.features.marketplace.MyStore.myproducts.ProductStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddProductViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val marketplaceRepository: MarketplaceRepository,
    private val fileHelper: FileHelper
) : ViewModel() {

    private val _state = MutableStateFlow(AddProductState())
    val state = _state.asStateFlow()

    private val _events = Channel<AddProductEvent>()
    val events = _events.receiveAsFlow()

    init {
        fetchCategories()
    }

    fun onAction(action: AddProductAction) {
        when (action) {
            is AddProductAction.OnNameChanged -> {
                _state.update {
                    it.copy(name = action.name, nameError = null)
                }
            }
            is AddProductAction.OnDescriptionChanged -> {
                _state.update {
                    it.copy(description = action.description, descriptionError = null)
                }
            }
            is AddProductAction.OnPriceChanged -> {
                _state.update {
                    it.copy(price = action.price, priceError = null)
                }
            }
            is AddProductAction.OnQuantityChanged -> {
                _state.update {
                    it.copy(quantity = action.quantity, quantityError = null)
                }
            }
            is AddProductAction.OnCategorySelected -> {
                _state.update {
                    it.copy(selectedCategoryId = action.categoryId, categoryError = null)
                }
            }
            is AddProductAction.OnUnitSelected -> {
                _state.update {
                    it.copy(selectedUnit = action.unit, unitError = null)
                }
            }
            is AddProductAction.OnImagesSelected -> {
                viewModelScope.launch {
                    _state.update { it.copy(isLoading = true) }
                    
                    val (uploadedUrls, uploadFailed) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        val urls = mutableListOf<Uri>()
                        var failed = false

                        for (uri in action.uris) {
                            // If it is already a web URL, skip upload
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
                            
                            // Clean up cache file immediately
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
                        Pair(urls, failed)
                    }

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
                        _events.send(AddProductEvent.ShowSnackbar("Failed to upload some images"))
                    }
                }
            }
            is AddProductAction.OnRemoveImage -> {
                _state.update {
                    val updatedList = it.selectedImageUris.filterNot { uri -> uri == action.uri }
                    it.copy(selectedImageUris = updatedList)
                }
            }
            AddProductAction.OnSubmitClicked -> {
                submitProduct()
            }
        }
    }

    private fun submitProduct() {
        val currentState = state.value
        
        // Form field validations
        val isNameValid = currentState.name.isNotBlank()
        val isDescriptionValid = currentState.description.isNotBlank()
        
        val parsedPrice = currentState.price.toDoubleOrNull()
        val isPriceValid = parsedPrice != null && parsedPrice > 0.0
        
        val parsedQuantity = currentState.quantity.toIntOrNull()
        val isQuantityValid = parsedQuantity != null && parsedQuantity > 0
        
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
                        parsedQuantity <= 0 -> "Quantity must be greater than zero"
                        else -> null
                    },
                    categoryError = if (isCategoryValid) null else "Please select a category",
                    unitError = if (isUnitValid) null else "Please select a unit of measurement",
                    imagesError = if (isImagesValid) null else "At least one product image is required"
                )
            }
            
            viewModelScope.launch {
                _events.send(AddProductEvent.ShowSnackbar("Please fix the validation errors in the form."))
            }
            return
        }

        // Two-Step submission process flow
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            // uploads raw images to backend

            val (uploadedUrls, uploadFailed) = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val urls = mutableListOf<String>()
                var failed = false

                for (uri in currentState.selectedImageUris) {
                    // If it is already a web URL, don't re-upload
                    if (uri.scheme == "http" || uri.scheme == "https") {
                        urls.add(uri.toString())
                        continue
                    }

                    val tempFile = fileHelper.uriToFile(uri)
                    if (tempFile == null) {
                        failed = true
                        break
                    }

                    val part = fileHelper.fileToMultipartPart(tempFile)
                    val uploadResult = marketplaceRepository.uploadImage(part)
                    
                    // Clean up cache file immediately
                    tempFile.delete()

                    when (uploadResult) {
                        is Result.Success -> {
                            urls.add(uploadResult.data.url)
                        }
                        is Result.Error -> {
                            failed = true
                            break
                        }
                    }
                }
                Pair(urls, failed)
            }

            if (uploadFailed) {
                _state.update { it.copy(isLoading = false) }
                _events.send(AddProductEvent.ShowSnackbar("Failed to upload images"))
                return@launch
            }

            // Log final collection of URLs
            android.util.Log.d("AddProductViewModel", "Uploaded image URLs: $uploadedUrls")

            //  Submit final JSON payload to Product API ---
            val request = ProductUploadRequest(
                productName = currentState.name,
                description = currentState.description,
                price = parsedPrice ?: 0.0,
                quantity = parsedQuantity ?: 0,
                unit = currentState.selectedUnit,
                categoryId = currentState.selectedCategoryId ?: "",
                images = uploadedUrls
            )

            val apiResult = marketplaceRepository.createProduct(request)

            _state.update { it.copy(isLoading = false) }

            when (apiResult) {
                is Result.Success -> {
                    _events.send(AddProductEvent.ShowSnackbar("Product successfully added to the marketplace!"))
                    _events.send(AddProductEvent.MapsBack)
                }
                is Result.Error -> {
                    _events.send(AddProductEvent.ShowSnackbar("Failed to submit product: ${apiResult.error}"))
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
                        AddProductEvent.ShowSnackbar("Failed to fetch categories: ${result.error}")
                    )
                }
            }
        }
    }
}
