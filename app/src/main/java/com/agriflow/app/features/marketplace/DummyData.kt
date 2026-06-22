/**
 * Represents the object [DummyData] providing core functionality within the application.
 */
package com.agriflow.app.features.marketplace

import com.agriflow.app.features.products.productdetails.Product

object DummyData {
    val sampleProducts = listOf(
        Product(
            id = "1",
            name = "Premium White Maize",
            category = "Grains",
            priceCents = 320000L,
            currencyCode = "KES",
            farmerName = "Eldoret Farms Ltd",
            companyName = "",
            imageUrl = "",
            availableQuantity = 150.0,
            quantityUnit = "bag",
            description = "High-quality white maize harvested from the fertile soil of Eldoret.",
            businessId = "1"
        ),
        Product(
            id = "2",
            name = "Fresh Hass Avocados",
            category = "Fruits",
            priceCents = 80000L,
            currencyCode = "KES",
            farmerName = "Jane Muthoni",
            companyName = "",
            imageUrl = "",
            availableQuantity = 50.0,
            quantityUnit = "kg",
            description = "Grade-A Hass avocados, hand-picked and perfect for wholesale.",
            businessId = "2"
        ),
        Product(
            id = "3",
            name = "Organic Tomatoes",
            category = "Vegetables",
            priceCents = 150000L,
            currencyCode = "KES",
            farmerName = "Naivasha Greenhouses",
            companyName = "",
            imageUrl = "",
            availableQuantity = 20.0,
            quantityUnit = "crate",
            description = "Crisp, organic vine-ripened tomatoes grown under standard greenhouse environments.",
            businessId = "3"
        ),
        Product(
            id = "4",
            name = "NPK Fertilizer 50kg",
            category = "Inputs",
            priceCents = 450000L,
            currencyCode = "KES",
            farmerName = "AgriSupply Co.",
            companyName = "",
            imageUrl = "",
            availableQuantity = 500.0,
            quantityUnit = "bag",
            description = "Standard NPK 17-17-17 fertilizer composition to optimize agricultural yields.",
            businessId = "4"
        ),
        Product(
            id = "5",
            name = "Sorghum (Red)",
            category = "Grains",
            priceCents = 280000L,
            currencyCode = "KES",
            farmerName = "Western Yields",
            companyName = "",
            imageUrl = "",
            availableQuantity = 80.0,
            quantityUnit = "bag",
            description = "Sun-dried red sorghum grains ideal for wholesale processing.",
            businessId = "5"
        ),
        Product(
            id = "6",
            name = "Export-Grade Mangoes",
            category = "Fruits",
            priceCents = 120000L,
            currencyCode = "KES",
            farmerName = "Coast Organics",
            companyName = "",
            imageUrl = "",
            availableQuantity = 35.0,
            quantityUnit = "kg",
            description = "Sweet, juicy coastal Apple mangoes ready for immediate export.",
            businessId = "6"
        ),
        Product(
            id = "7",
            name = "Premium Milk",
            category = "Dairy",
            priceCents = 5000L,
            currencyCode = "KES",
            farmerName = "KICC DAIRY",
            companyName = "",
            imageUrl = "",
            availableQuantity = 1000.0,
            quantityUnit = "ltr",
            description = "Pasteurized fresh cow milk from standard local dairy farms.",
            businessId = "7"
        ),
        Product(
            id = "8",
            name = "insecticide",
            category = "Chemicals",
            priceCents = 100000L,
            currencyCode = "KES",
            farmerName = "Synngenta",
            companyName = "",
            imageUrl = "",
            availableQuantity = 500000.0,
            quantityUnit = "ltr",
            description = "Eco-safe agricultural insecticide certified for standard crop protection.",
            businessId = "8"
        )
    )
}