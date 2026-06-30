package com.agriflow.app.features.ratings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agriflow.app.core.util.DataError
import com.agriflow.app.core.util.Result
import com.agriflow.app.features.ratings.PendingReview
import com.agriflow.app.features.ratings.SellerRating
import com.agriflow.app.features.ratings.RatingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val ratingsRepository: RatingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RatingsUiState())
    val uiState: StateFlow<RatingsUiState> = _uiState.asStateFlow()

    fun getRatingsForSeller(sellerId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = ratingsRepository.getRatingsForSeller(sellerId)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, ratings = result.data) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error) }
                }
            }
        }
    }

    fun submitRating(sellerId: String, ratingValue: Float, reviewText: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitError = null, submitSuccess = false) }
            when (val result = ratingsRepository.submitRating(sellerId, ratingValue, reviewText)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSubmitting = false, submitSuccess = true) }
                    // Optionally refresh the list
                    getRatingsForSeller(sellerId)
                    // Also refresh pending reviews as we just submitted one
                    checkPendingReviews()
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSubmitting = false, submitError = result.error) }
                }
            }
        }
    }
    
    fun checkPendingReviews() {
        viewModelScope.launch {
            when (val result = ratingsRepository.getPendingReviews()) {
                is Result.Success -> {
                    _uiState.update { it.copy(pendingReviews = result.data) }
                }
                is Result.Error -> {
                    // Fail silently for pending reviews, or log it
                }
            }
        }
    }

    fun resetSubmitState() {
        _uiState.update { it.copy(submitSuccess = false, submitError = null) }
    }
}
