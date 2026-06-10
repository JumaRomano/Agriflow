package com.agriflow.app.features.marketplace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.util.Result
import com.agriflow.app.core.security.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val repository: MarketplaceRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedCategory = MutableStateFlow("All")
    private val _isRefreshing = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _state = MutableStateFlow(MarketplaceState())
    val state = _state.asStateFlow()

    private val _events = Channel<MarketplaceEvent>()
    val events = _events.receiveAsFlow()

    private val _filteredProducts = combine(
        repository.observeProducts(),
        _searchQuery,
        _selectedCategory
    ) { products, query, category ->
        products.filter { product ->
            val matchesCategory = category == "All" || product.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() || product.name.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    init {
        refreshProducts()

        // 1. Observe filtered products list
        viewModelScope.launch {
            _filteredProducts.collect { list ->
                _state.update { it.copy(products = list) }
            }
        }

        // 2. Observe search query changes
        viewModelScope.launch {
            _searchQuery.collect { query ->
                _state.update { it.copy(searchQuery = query) }
            }
        }

        // 3. Observe category selection changes
        viewModelScope.launch {
            _selectedCategory.collect { category ->
                _state.update { it.copy(selectedCategory = category) }
            }
        }

        // 4. Observe refreshing state changes
        viewModelScope.launch {
            _isRefreshing.collect { refreshing ->
                _state.update { it.copy(isRefreshing = refreshing) }
            }
        }

        // 5. Observe error message changes
        viewModelScope.launch {
            _errorMessage.collect { error ->
                _state.update { it.copy(errorMessage = error) }
            }
        }

        // 6. Observe user role flow changes
        viewModelScope.launch {
            tokenRepository.getUserRoleFlow().collect { role ->
                _state.update { it.copy(userRole = role) }
            }
        }
    }

    fun onAction(action: MarketplaceAction) {
        when (action) {
            is MarketplaceAction.SearchQueryChanged -> {
                _searchQuery.value = action.query
            }
            is MarketplaceAction.CategorySelected -> {
                _selectedCategory.value = action.category
            }
            MarketplaceAction.RefreshRequested -> {
                refreshProducts()
            }
            is MarketplaceAction.ProductClicked -> {
                viewModelScope.launch {
                    _events.send(MarketplaceEvent.NavigateToProductDetails(action.product.id))
                }
            }
            MarketplaceAction.CartClicked -> {
                viewModelScope.launch {
                    _events.send(MarketplaceEvent.NavigateToCart)
                }
            }
        }
    }

    private fun refreshProducts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _errorMessage.value = null
            
            when (repository.refreshProducts()) {
                is Result.Success -> {
                    // Success is handled automatically by the Room flow update
                }
                is Result.Error -> {
                    _errorMessage.value = "Failed to refresh products."
                    _events.send(MarketplaceEvent.ShowMessage("Failed to sync with server. You are viewing offline data."))
                }
            }
            _isRefreshing.value = false
        }
    }
}
