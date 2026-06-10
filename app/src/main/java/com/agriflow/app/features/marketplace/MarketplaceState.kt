package com.agriflow.app.features.marketplace

import com.agriflow.app.features.auth.UserRole

data class MarketplaceState(
    val products: List<Product> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String = "All",
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val userRole: UserRole = UserRole.UNKNOWN
) {
    val availableCategories = listOf("All", "Grains", "Vegetables", "Fruits", "Livestock", "Dairy", "Poultry", "Herbs")
}
