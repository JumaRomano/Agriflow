/**
 * Sealed interface representing user actions and UI events for the AddProduct flow.
 */
package com.agriflow.app.features.products.addproduct

import android.net.Uri

sealed interface AddProductAction {
    data class OnNameChanged(val name: String) : AddProductAction
    data class OnDescriptionChanged(val description: String) : AddProductAction
    data class OnPriceChanged(val price: String) : AddProductAction
    data class OnQuantityChanged(val quantity: String) : AddProductAction
    data class OnCategorySelected(val categoryId: String) : AddProductAction
    data class OnUnitSelected(val unit: String) : AddProductAction
    data class OnImagesSelected(val uris: List<Uri>) : AddProductAction
    data class OnRemoveImage(val uri: Uri) : AddProductAction
    data object OnSubmitClicked : AddProductAction
}
