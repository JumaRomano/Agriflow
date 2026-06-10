package com.agriflow.app.features.marketplace

import com.agriflow.app.features.marketplace.ProductEntity
import com.agriflow.app.features.marketplace.ProductDto
import com.agriflow.app.features.marketplace.Product
fun ProductDto.toEntity(nowMillis: Long): ProductEntity? {
    val id = id?.takeIf(String::isNotBlank) ?: return null
    val name = name?.takeIf(String::isNotBlank) ?: return null
    val category = category?.takeIf(String::isNotBlank) ?: return null
    
    val priceDouble = price ?: return null
    val priceCents = (priceDouble * 100).toLong()

    val companyName = companyName?.takeIf(String::isNotBlank) ?: "Independent Seller"
    val farmerName = companyName // Fallback to companyName as businessName holds the farmer's branding

    val availableQuantity = availableQuantity ?: return null
    val imageUrl = images?.firstOrNull()?.replace("http://", "https://")
    val desc = description.orEmpty()

    return ProductEntity(
        id = id,
        name = name,
        category = category,
        priceCents = priceCents,
        currencyCode = DEFAULT_CURRENCY_CODE,
        farmerName = farmerName,
        imageUrl = imageUrl?.takeIf(String::isNotBlank),
        availableQuantity = availableQuantity,
        quantityUnit = quantityUnit?.takeIf(String::isNotBlank) ?: DEFAULT_QUANTITY_UNIT,
        updatedAtMillis = nowMillis,
        companyName = companyName,
        description = desc
    )
}

fun ProductEntity.toDomain(): Product {
    return Product(
        id = id,
        name = name,
        category = category,
        priceCents = priceCents,
        currencyCode = currencyCode,
        farmerName = farmerName,
        imageUrl = imageUrl,
        availableQuantity = availableQuantity,
        quantityUnit = quantityUnit,
        companyName = companyName,
        description = description
    )
}

private const val DEFAULT_CURRENCY_CODE = "KES"
private const val DEFAULT_QUANTITY_UNIT = "kg"
