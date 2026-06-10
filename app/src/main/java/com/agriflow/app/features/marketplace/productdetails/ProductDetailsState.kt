package com.agriflow.app.features.marketplace.productdetails

import com.agriflow.app.features.auth.UserRole
import com.agriflow.app.features.marketplace.Product

data class ProductDetailsState(
    val product: Product? = null,
    val selectedQuantity: Int = 1,
    val isLoading: Boolean = true,
    val userRole: UserRole = UserRole.BUYER
)
