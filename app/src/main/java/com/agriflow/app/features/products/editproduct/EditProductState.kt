/**
 * UI State definition representing the screen state for EditProduct.
 */
package com.agriflow.app.features.products.editproduct

import android.net.Uri
import com.agriflow.app.features.marketplace.CategoryDto

data class EditProductState(
    val productId: String = "",
    val name: String = "",
    val description: String = "",
    val price: String = "",
    val quantity: String = "",
    val selectedCategoryId: String? = null,
    val selectedUnit: String = "",
    val selectedImageUris: List<Uri> = emptyList(),
    val isLoading: Boolean = false,
    val isFetchingCategories: Boolean = false,
    val success: Boolean = false,
    
    // Master data
    val categories: List<CategoryDto> = emptyList(),
    val units: List<String> = listOf("kg", "ton", "bag", "liter", "crate"),
    
    // Input validation errors
    val nameError: String? = null,
    val descriptionError: String? = null,
    val priceError: String? = null,
    val quantityError: String? = null,
    val categoryError: String? = null,
    val unitError: String? = null,
    val imagesError: String? = null
)
