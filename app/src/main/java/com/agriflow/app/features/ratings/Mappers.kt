package com.agriflow.app.features.ratings

fun SellerRatingDto.toSellerRating(): SellerRating {
    return SellerRating(
        id = id,
        businessId = businessId,
        buyerId = buyerId,
        buyerName = buyerName,
        ratingValue = ratingValue,
        reviewText = reviewText,
        timestamp = timestamp
    )
}

fun PendingReviewDto.toPendingReview(): PendingReview {
    return PendingReview(
        orderId = orderId,
        businessId = businessId,
        sellerName = sellerName,
        orderCompletedAt = orderCompletedAt
    )
}

fun BusinessRatingSummaryDto.toBusinessRatingSummary(fallbackBusinessId: String = ""): BusinessRatingSummary {
    return BusinessRatingSummary(
        businessId = businessId ?: fallbackBusinessId,
        averageRating = averageRating ?: 0.0,
        totalRatings = totalRatings ?: 0
    )
}

fun RatingDto.toRating(): Rating {
    return Rating(
        id = id.orEmpty(),
        businessId = businessId,
        productId = productId,
        userId = userId ?: buyerId,
        buyerName = buyerName,
        ratingValue = ratingValue ?: rating?.toFloatOrNull() ?: 0f,
        comment = comment ?: reviewText,
        timestamp = timestamp ?: 0L
    )
}
