/**
 * UI State definition representing the screen state for Home.
 */
package com.agriflow.app.homescreen

import com.agriflow.app.features.auth.UserRole
import com.agriflow.app.features.products.productdetails.Product

/**
 * State representing the Home Dashboard screen.
 */
data class HomeCategory(
    val id: String,
    val name: String,
    val isSelected: Boolean = false,
    val emoji: String = ""
)

/**
 * State representing the Home Dashboard screen.
 */
data class HomeState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val userRole: UserRole = UserRole.BUYER,
    val name: String = "John Doe",
    val products: List<Product> = emptyList(),
    val categories: List<HomeCategory> = emptyList(),
    val distributors: List<Distributor> = emptyList(),
    val selectedCategory: String = "All",
    val searchQuery: String = ""
)

