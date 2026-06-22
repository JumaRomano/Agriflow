/**
 * ViewModel managing the business logic and UI state for the Home feature.
 */
package com.agriflow.app.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.agriflow.app.features.marketplace.MarketplaceRepository
import javax.inject.Inject
import kotlin.text.equals
import com.agriflow.app.core.util.Result

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {

    private val initialCategories = listOf(
        HomeCategory("all", "All", isSelected = true)
    )

    private val _state = MutableStateFlow(
        HomeState(
            categories = initialCategories,
        )
    )
    val state = _state.asStateFlow()

    private val _events = Channel<HomeEvent>()
    val events = _events.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")

    init {
        // Collect user role
        viewModelScope.launch {
            tokenRepository.getUserRoleFlow().collect { role ->
                _state.update { it.copy(userRole = role) }
            }
        }
        
        // Collect username
        viewModelScope.launch {
            tokenRepository.getUserFlow().collect { user ->
                if (user != null) {
                    _state.update {
                        it.copy(
                            name = user.username,
                        )
                    }
                }
            }
        }

        // Observe marketplace products
        viewModelScope.launch {
            marketplaceRepository.observeProducts().collect { list ->
                _state.update { it.copy(products = list) }
            }
        }

        // Sync products from server
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            marketplaceRepository.refreshProducts()
            _state.update { it.copy(isLoading = false) }
        }

        // search
        viewModelScope.launch {
            _searchQuery.collect { query ->
                _state.update { it.copy(searchQuery = query) }
            }
        }
        // observe  filtered products
        
        fetchCategories()
        fetchVerifiedBusinesses()
    }

    fun onAction(action: HomeAction) {
        when (action) {

            HomeAction.CartClicked -> {
                viewModelScope.launch {
                    _events.send(HomeEvent.NavigateToCart)
                }
            }
            HomeAction.NotificationsClicked -> {
                viewModelScope.launch {
                    _events.send(HomeEvent.NavigateToNotification)
                }
            }
            HomeAction.WalletClicked -> {
                viewModelScope.launch {
                    _events.send(HomeEvent.NavigateToWallet)
                }
            }

            is HomeAction.SearchQueryChanged -> {
                _searchQuery.value = action.query
            }

            is HomeAction.CategorySelected -> {
                _state.update { currentState ->
                    val updatedCategories = currentState.categories.map { category ->
                        category.copy(isSelected = category.name.equals(action.category, ignoreCase = true))
                    }
                    currentState.copy(
                        selectedCategory = action.category,
                        categories = updatedCategories
                    )
                }
            }

            is HomeAction.ProductClicked -> {
                viewModelScope.launch {
                    _events.send(HomeEvent.NavigateToProductDetails(action.product.id))
                }
            }

            is HomeAction.DistributorClicked -> {
                viewModelScope.launch {
                    _events.send(HomeEvent.NavigateToBusinessDetails(action.businessId))
                }
            }

            HomeAction.StartSourcingClicked -> {
                viewModelScope.launch {
                    _events.send(HomeEvent.NavigateToMarketplace)
                }
            }

            HomeAction.ViewSupplierNetworkClicked -> {
                viewModelScope.launch {
                    _events.send(HomeEvent.NavigateToSupplierNetwork)
                }
            }
        }
    }
    private fun fetchCategories() {
        viewModelScope.launch {
            when (val result = marketplaceRepository.getCategories()) {
                is Result.Success -> {
                    val apiCategories = result.data.map { dto ->
                        HomeCategory(
                            id = dto.id,
                            name = dto.name,
                            isSelected = false
                        )
                    }
                    val combinedCategories = if (apiCategories.isEmpty()) {
                        initialCategories
                    } else {
                        initialCategories + apiCategories
                    }
                    _state.update { currentState ->
                        currentState.copy(
                            categories = combinedCategories,
                            selectedCategory = if (currentState.selectedCategory.isEmpty()) "All" else currentState.selectedCategory
                        )
                    }
                }
                is Result.Error -> {
                    _state.update { currentState ->
                        currentState.copy(
                            categories = initialCategories,
                            selectedCategory = if (currentState.selectedCategory.isEmpty()) "All" else currentState.selectedCategory
                        )
                    }
                }
            }
        }
    }

    private fun fetchVerifiedBusinesses() {
        viewModelScope.launch {
            when (val result = marketplaceRepository.getVerifiedBusinesses()) {
                is Result.Success -> {
                    val distributors = result.data.mapNotNull { dto ->
                        val id = dto.id ?: return@mapNotNull null
                        val name = dto.name ?: return@mapNotNull null
                        Distributor(
                            id = id,
                            brandName = name,
                            tagline = dto.tagline ?: "Verified AgriFlow partner",
                            logoUrl = dto.logoUrl,
                            rating = dto.rating ?: 5.0,
                            reviewCount = dto.reviewCount ?: 0,
                            isVerified = dto.isVerified ?: true,
                            emoji = if (dto.type?.equals("FARMER", ignoreCase = true) == true) "🌾" else "🏢"
                        )
                    }
                    _state.update { currentState ->
                        currentState.copy(distributors = distributors)
                    }
                }
                is Result.Error -> {
                    _state.update { currentState ->
                        currentState.copy(distributors = emptyList())
                    }
                }
            }
        }
    }
}
