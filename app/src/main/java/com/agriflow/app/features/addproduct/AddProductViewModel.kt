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
        val routeArgs = savedStateHandle.toRoute<Route.AddProduct>()
        val productId = routeArgs.productId
        if (productId != null) {
            val mockProduct = getMockProductById(productId)
            if (mockProduct != null) {
                _state.update {
                    it.copy(
                        productId = productId,
                        name = mockProduct.name,
                        description = when (mockProduct.id) {
                            "prod-001" -> "High-yield Premium White Maize. Hand-sorted and dried to optimum moisture level."
                            "prod-002" -> "Export-quality fresh Hass Avocados, direct from orchards. Rich, creamy texture."
                            "prod-003" -> "Plump and juicy vine-ripened organic tomatoes. Grown with eco-friendly practices."
                            "prod-004" -> "Balanced NPK fertilizer 17-17-17 formulation. Perfect for crop establishment and root development."
                            "prod-005" -> "Organic eco-friendly biopesticide spray. Effective against major pests without harming pollinators."
                            else -> "Premium agricultural product. High-grade and ready for delivery."
                        },
                        price = mockProduct.price.toString(),
                        quantity = mockProduct.stockQuantity.toString(),
                        selectedCategoryId = when (mockProduct.id) {
                            "prod-001" -> "cat-grains"
                            "prod-002" -> "cat-fruits"
                            "prod-003" -> "cat-vegetables"
                            "prod-004" -> "cat-inputs"
                            "prod-005" -> "cat-chemicals"
                            else -> null
                        },
                        selectedUnit = mockProduct.unit,
                        selectedImageUris = if (mockProduct.imageUrl.isNotEmpty()) {
                            listOf(Uri.parse(mockProduct.imageUrl))
                        } else emptyList()
                    )
                }
            }
        }
    }

    private fun getMockProductById(id: String): ListedProduct? {
        val mockProducts = listOf(
            ListedProduct(
                id = "prod-001",
                name = "Premium White Maize",
                price = 3200.0,
                stockQuantity = 150,
                unit = "bag",
                imageUrl = "https://picsum.photos/id/1080/200/200",
                status = ProductStatus.ACTIVE
            ),
            ListedProduct(
                id = "prod-002",
                name = "Fresh Hass Avocados",
                price = 800.0,
                stockQuantity = 50,
                unit = "kg",
                imageUrl = "https://picsum.photos/id/106/200/200",
                status = ProductStatus.ACTIVE
            ),
            ListedProduct(
                id = "prod-003",
                name = "Organic Tomatoes",
                price = 1500.0,
                stockQuantity = 0,
                unit = "crate",
                imageUrl = "https://picsum.photos/id/429/200/200",
                status = ProductStatus.OUT_OF_STOCK
            ),
            ListedProduct(
                id = "prod-004",
                name = "NPK Fertilizer 50kg",
                price = 4500.0,
                stockQuantity = 500,
                unit = "bag",
                imageUrl = "https://picsum.photos/id/1080/200/200",
                status = ProductStatus.ACTIVE
            ),
            ListedProduct(
                id = "prod-005",
                name = "Bio-Pesticide Eco-Spray",
                price = 1150.0,
                stockQuantity = 12,
                unit = "liter",
                imageUrl = "https://picsum.photos/id/106/200/200",
                status = ProductStatus.UNDER_REVIEW
            )
        )
        return mockProducts.find { it.id == id }
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
            
            // --- STEP 1: Upload Raw Image Uris to Backend ---
            val isEditMode = currentState.productId != null

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

            // --- STEP 2: Submit final JSON payload to Product API ---
            val request = ProductUploadRequest(
                productName = currentState.name,
                description = currentState.description,
                price = parsedPrice ?: 0.0,
                quantity = parsedQuantity ?: 0,
                unit = currentState.selectedUnit,
                categoryId = currentState.selectedCategoryId ?: "",
                images = uploadedUrls
            )

            val apiResult = if (isEditMode) {
                marketplaceRepository.updateProduct(currentState.productId!!, request)
            } else {
                marketplaceRepository.createProduct(request)
            }

            _state.update { it.copy(isLoading = false) }

            when (apiResult) {
                is Result.Success -> {
                    val successMessage = if (isEditMode) "Product updated successfully!" else "Product successfully added to the marketplace!"
                    _events.send(AddProductEvent.ShowSnackbar(successMessage))
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
