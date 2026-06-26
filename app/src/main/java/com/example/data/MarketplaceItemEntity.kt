package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "marketplace_items")
data class MarketplaceItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val price: Double,
    val vendorEmail: String,
    val vendorName: String,
    val category: String, // e.g. "Burger", "Pizza", "Dessert", "Drinks"
    val timestamp: Long = System.currentTimeMillis()
)
