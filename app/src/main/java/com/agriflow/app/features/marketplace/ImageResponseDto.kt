package com.agriflow.app.features.marketplace

import com.google.gson.annotations.SerializedName

data class ImageResponseDto(
    @SerializedName("imageUrl")
    val url: String
)
