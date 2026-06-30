package com.agriflow.app.features.ratings

data class SellerRating(
    val id: String,
    val sellerId: String,
    val buyerId: String,
    val buyerName: String?,
    val ratingValue: Float,
    val reviewText: String?,
    val timestamp: Long
)

data class PendingReview(
    val orderId: String,
    val sellerId: String,
    val sellerName: String,
    val orderCompletedAt: Long
)
