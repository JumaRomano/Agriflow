/**
 * Core helper component: ProductMappers.
 */
package com.agriflow.app.features.products.productdetails

fun ProductDto.toEntity(nowMillis: Long): ProductEntity? {
    val id = id?.takeIf(String::isNotBlank) ?: return null
    val name = name?.takeIf(String::isNotBlank) ?: return null
    val category = category?.takeIf(String::isNotBlank) ?: return null
    
    val priceDouble = price ?: 0.0
    val priceCents = (priceDouble * 100).toLong()

    val companyName = companyName?.takeIf(String::isNotBlank) ?: "Independent Seller"
    val farmerName = companyName // Fallback to companyName as businessName holds the farmer's branding

    val stockVal = availableStock ?: availableQuantity ?: 0.0
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
        availableQuantity = stockVal,
        quantityUnit = quantityUnit?.takeIf(String::isNotBlank) ?: DEFAULT_QUANTITY_UNIT,
        updatedAtMillis = nowMillis,
        companyName = companyName,
        description = desc,
        businessId = businessId,
        availableStock = availableStock ?: availableQuantity,
        stockStatus = stockStatus
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
        description = description,
        businessId = businessId,
        availableStock = availableStock ?: availableQuantity,
        stockStatus = stockStatus
    )
}

private const val DEFAULT_CURRENCY_CODE = "KES"
private const val DEFAULT_QUANTITY_UNIT = "kg"
