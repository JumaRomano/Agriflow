package com.agriflow.app.features.wallet

import com.agriflow.app.core.util.TimeParser

fun WalletTransactionDto.toEntity(fallbackTimestamp: Long): TransactionEntity {
    val parsedTimestamp = createdAt?.let { TimeParser.parseIsoStringToMillis(it) } ?: fallbackTimestamp
    return TransactionEntity(
        transactionId = id ?: java.util.UUID.randomUUID().toString(),
        amount = amount ?: 0.0,
        type = type ?: "REVENUE",
        status = status ?: "COMPLETED",
        timestamp = parsedTimestamp,
        description = description.orEmpty(),
        category = category.orEmpty()
    )
}
