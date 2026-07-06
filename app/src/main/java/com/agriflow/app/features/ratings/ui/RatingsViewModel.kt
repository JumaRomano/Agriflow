package com.agriflow.app.features.ratings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.orders.OrderDto
import com.agriflow.app.features.ratings.PendingReview
import com.agriflow.app.features.ratings.SellerRating
import com.agriflow.app.features.ratings.Rating
import com.agriflow.app.features.ratings.RatingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface RatingState {
    object Loading : RatingState
    data class Unrated(val error: String? = null) : RatingState
    data class Rated(val ratingValue: Float, val comment: String?) : RatingState
}

data class RatingsUiState(
    val isLoading: Boolean = false,
    val ratings: List<SellerRating> = emptyList(),
    val error: DataError.Network? = null,
    val pendingReviews: List<PendingReview> = emptyList(),
    val isSubmitting: Boolean = false,
    val submitError: DataError.Network? = null,
    val submitSuccess: Boolean = false
)

@HiltViewModel
class RatingsViewModel @Inject constructor(
    private val ratingsRepository: RatingsRepository,
    private val tokenRepository: TokenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RatingsUiState())
    val uiState: StateFlow<RatingsUiState> = _uiState.asStateFlow()

    private val _itemStates = MutableStateFlow<Map<String, RatingState>>(emptyMap())
    val itemStates: StateFlow<Map<String, RatingState>> = _itemStates.asStateFlow()

    private var userId: String? = null
    private var orderUserId: String? = null

    init {
        viewModelScope.launch {
            tokenRepository.getUserFlow().collect { user ->
                userId = user?.id
            }
        }
    }

    fun getRatingsForBusiness(businessId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = ratingsRepository.getRatingsForBusiness(businessId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, ratings = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }

    fun submitRating(businessId: String, ratingValue: Float, reviewText: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null, submitSuccess = false) }
            when (val result = ratingsRepository.submitRating(businessId, ratingValue, reviewText)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                    getRatingsForBusiness(businessId)

                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, submitError = result.error) }
                }
            }
        }
    }
    


    fun deleteBusinessRating(businessId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = ratingsRepository.deleteBusinessRating(businessId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    getRatingsForBusiness(businessId)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }

    fun resetSubmitState() {
        _uiState.update { it.copy(submitSuccess = false, submitError = null) }
    }

    // --- NEW MULTI-VENDOR RATINGS METHODS ---

    fun loadRatingsForOrder(order: OrderDto) {
        orderUserId = order.userId
        val items = order.items.orEmpty()
        val businessIds = (items.mapNotNull { it.businessId } + listOfNotNull(order.businessId)).distinct()
        val productIds = items.mapNotNull { it.productId }.distinct()

        val initialMap = (businessIds + productIds).associateWith { RatingState.Loading }
        _itemStates.value = initialMap

        viewModelScope.launch {
            businessIds.forEach { bId ->
                launch {
                    fetchBusinessRating(bId)
                }
            }
            productIds.forEach { pId ->
                launch {
                    fetchProductRating(pId)
                }
            }
        }
    }

    private suspend fun fetchBusinessRating(businessId: String) {
        when (val result = ratingsRepository.getBusinessRatings(businessId)) {
            is Result.Success -> {
                val currentUid = userId ?: orderUserId
                val userRating = result.data.firstOrNull { it.userId == currentUid && it.userId != null }
                if (userRating != null) {
                    _itemStates.update { it + (businessId to RatingState.Rated(userRating.ratingValue, userRating.comment)) }
                } else {
                    _itemStates.update { it + (businessId to RatingState.Unrated(null)) }
                }
            }
            is Result.Error -> {
                _itemStates.update { it + (businessId to RatingState.Unrated("Error: ${result.error.name}")) }
            }
        }
    }

    private suspend fun fetchProductRating(productId: String) {
        when (val result = ratingsRepository.getProductRatings(productId)) {
            is Result.Success -> {
                val currentUid = userId ?: orderUserId
                val userRating = result.data.firstOrNull { it.userId == currentUid && it.userId != null }
                if (userRating != null) {
                    _itemStates.update { it + (productId to RatingState.Rated(userRating.ratingValue, userRating.comment)) }
                } else {
                    _itemStates.update { it + (productId to RatingState.Unrated(null)) }
                }
            }
            is Result.Error -> {
                _itemStates.update { it + (productId to RatingState.Unrated("Error: ${result.error.name}")) }
            }
        }
    }

    fun submitBusinessRating(businessId: String, rating: Float, comment: String) {
        _itemStates.update { it + (businessId to RatingState.Loading) }
        viewModelScope.launch {
            when (val result = ratingsRepository.submitBusinessRating(businessId, rating, comment)) {
                is Result.Success -> {
                    _itemStates.update { it + (businessId to RatingState.Rated(rating, comment)) }
                }
                is Result.Error -> {
                    _itemStates.update { it + (businessId to RatingState.Unrated("Submit failed: ${result.error.name}")) }
                }
            }
        }
    }

    fun submitProductRating(productId: String, rating: Float, comment: String) {
        val currentUid = userId ?: orderUserId
        if (currentUid == null) {
            _itemStates.update { it + (productId to RatingState.Unrated("User not logged in")) }
            return
        }
        _itemStates.update { it + (productId to RatingState.Loading) }
        viewModelScope.launch {
            when (val result = ratingsRepository.submitProductRating(currentUid, productId, rating, comment)) {
                is Result.Success -> {
                    _itemStates.update { it + (productId to RatingState.Rated(rating, comment)) }
                }
                is Result.Error -> {
                    _itemStates.update { it + (productId to RatingState.Unrated("Submit failed: ${result.error.name}")) }
                }
            }
        }
    }

    fun deleteBusinessRatingNew(businessId: String) {
        _itemStates.update { it + (businessId to RatingState.Loading) }
        viewModelScope.launch {
            when (val result = ratingsRepository.deleteBusinessRatingNew(businessId)) {
                is Result.Success -> {
                    _itemStates.update { it + (businessId to RatingState.Unrated(null)) }
                }
                is Result.Error -> {
                    _itemStates.update { it + (businessId to RatingState.Unrated("Delete failed: ${result.error.name}")) }
                }
            }
        }
    }

    fun deleteProductRating(productId: String) {
        _itemStates.update { it + (productId to RatingState.Loading) }
        viewModelScope.launch {
            when (val result = ratingsRepository.deleteProductRating(productId)) {
                is Result.Success -> {
                    _itemStates.update { it + (productId to RatingState.Unrated(null)) }
                }
                is Result.Error -> {
                    _itemStates.update { it + (productId to RatingState.Unrated("Delete failed: ${result.error.name}")) }
                }
            }
        }
    }
}
