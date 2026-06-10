package com.agriflow.app.features.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.features.marketplace.MarketplaceAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.agriflow.app.features.marketplace.MarketplaceRepository
import kotlinx.coroutines.flow.collect
import javax.inject.Inject
import kotlin.text.equals

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val marketplaceRepository: MarketplaceRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
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

            is HomeAction.SearchQueryChanged -> {
                _searchQuery.value = action.query
            }


            is HomeAction.ProductClicked -> {
                viewModelScope.launch {
                    _events.send(HomeEvent.NavigateToProductDetails(action.product.id))
                }
            }
            else -> {}
        }
    }


}
