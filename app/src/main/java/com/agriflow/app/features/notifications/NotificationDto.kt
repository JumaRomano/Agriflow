package com.agriflow.app.features.notifications

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName(value = "body", alternate = ["message", "description"]) val body: String?,
    @SerializedName(value = "read", alternate = ["isRead"]) val read: Boolean?,
    @SerializedName("createdAt") val createdAt: String?
)
