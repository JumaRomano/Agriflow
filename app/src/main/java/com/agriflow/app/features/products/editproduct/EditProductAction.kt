/**
 * Sealed interface representing user actions and UI events for the EditProduct flow.
 */
package com.agriflow.app.features.products.editproduct

import android.net.Uri

sealed interface EditProductAction {
    data class OnNameChanged(val name: String) : EditProductAction
    data class OnDescriptionChanged(val description: String) : EditProductAction
    data class OnPriceChanged(val price: String) : EditProductAction
    data class OnQuantityChanged(val quantity: String) : EditProductAction
    data class OnCategorySelected(val categoryId: String) : EditProductAction
    data class OnUnitSelected(val unit: String) : EditProductAction
    data class OnImagesSelected(val uris: List<Uri>) : EditProductAction
    data class OnRemoveImage(val uri: Uri) : EditProductAction
    data object OnSaveClicked : EditProductAction
    data object OnMarkOutOfStockClicked : EditProductAction
}
