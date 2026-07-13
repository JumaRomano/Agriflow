/**
 * Represents a distributor in the AgriFlow marketplace.
 */
package com.agriflow.app.homescreen

data class Distributor(
    val id: String,
    val brandName: String,
    val tagline: String,
    val logoUrl: String? = null,
    val rating: Double? = null,
    val reviewCount: Int = 0,
    val isVerified: Boolean = true,
    val emoji: String = "🏢"
)
