package com.agriflow.app.features.notifications

import com.google.gson.annotations.SerializedName

data class DeviceTokenRequestDto(
    @SerializedName("fcmToken") val fcmToken: String,
    @SerializedName("deviceType") val deviceType: String
)
