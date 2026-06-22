/**
 * UI State definition representing the screen state for ProductDetails.
 */
package com.agriflow.app.features.products.productdetails

import com.agriflow.app.features.auth.UserRole

data class ProductDetailsState(
    val product: Product? = null,
    val selectedQuantity: Int = 1,
    val isLoading: Boolean = true,
    val userRole: UserRole = UserRole.BUYER
)
