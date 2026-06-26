package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val itemId: Int,
    val itemTitle: String,
    val itemPrice: Double,
    val foodieEmail: String,
    val vendorEmail: String,
    val status: String, // "PENDING", "PREPARING", "READY", "DELIVERED"
    val timestamp: Long = System.currentTimeMillis()
)
