package com.agriflow.app.features.staff.auth

data class AssignmentsDto(
    val id: String,
    val staffId: String,
    val businessId: String,
    val status: String,
    val createdAt: String,
    val updatedAt: String
)