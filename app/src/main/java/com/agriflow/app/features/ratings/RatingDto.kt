package com.agriflow.app.features.ratings

import kotlinx.serialization.Serializable

@Serializable
data class SellerRatingDto(
    val id: String,
    val sellerId: String,
    val buyerId: String,
    val buyerName: String?,
    val ratingValue: Float,
    val reviewText: String?,
    val timestamp: Long
)

@Serializable
data class SubmitRatingRequestDto(
    val sellerId: String,
    val ratingValue: Float,
    val reviewText: String?
)

@Serializable
data class PendingReviewDto(
    val orderId: String,
    val sellerId: String,
    val sellerName: String,
    val orderCompletedAt: Long
)
