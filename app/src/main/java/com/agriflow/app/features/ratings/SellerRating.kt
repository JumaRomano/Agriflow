package com.agriflow.app.features.ratings

data class SellerRating(
    val id: String,
    val businessId: String,
    val buyerId: String,
    val buyerName: String?,
    val ratingValue: Float,
    val reviewText: String?,
    val timestamp: Long
)

data class PendingReview(
    val orderId: String,
    val businessId: String,
    val sellerName: String,
    val orderCompletedAt: Long
)

data class BusinessRatingSummary(
    val businessId: String,
    val averageRating: Double,
    val totalRatings: Int
)

data class Rating(
    val id: String,
    val businessId: String?,
    val productId: String?,
    val userId: String?,
    val buyerName: String?,
    val ratingValue: Float,
    val comment: String?,
    val timestamp: Long
)
