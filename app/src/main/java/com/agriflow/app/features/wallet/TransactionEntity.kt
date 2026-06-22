package com.agriflow.app.features.wallet

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity representing a transaction in the Wallet feature.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val transactionId: String,
    val amount: Double,
    val type: String, // "Credit" or "Debit"
    val status: String, // "PENDING", "COMPLETED", "FAILED"
    val timestamp: Long,
    val description: String,
    val category: String
)
