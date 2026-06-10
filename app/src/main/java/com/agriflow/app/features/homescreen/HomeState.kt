package com.agriflow.app.features.homescreen

import com.agriflow.app.features.auth.UserRole
import com.agriflow.app.features.marketplace.Product

/**
 * State representing the Home Dashboard screen.
 */
data class HomeState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userRole: UserRole = UserRole.BUYER,
    val name: String = "John Doe",
    val products: List<Product> = emptyList(),
    val searchQuery: String = ""
)

