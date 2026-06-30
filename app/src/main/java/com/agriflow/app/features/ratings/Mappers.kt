package com.agriflow.app.features.ratings

fun SellerRatingDto.toSellerRating(): SellerRating {
    return SellerRating(
        id = id,
        sellerId = sellerId,
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
        sellerId = sellerId,
        sellerName = sellerName,
        orderCompletedAt = orderCompletedAt
    )
}
