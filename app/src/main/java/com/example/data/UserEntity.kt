package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val email: String,
    val passwordHash: String,
    val role: String, // "FOODIE" or "VENDOR"
    val displayName: String,
    val isGoogleUser: Boolean = false
)
