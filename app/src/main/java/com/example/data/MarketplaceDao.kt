package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketplaceDao {

    // --- USERS ---
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // --- MARKETPLACE ITEMS ---
    @Query("SELECT * FROM marketplace_items ORDER BY timestamp DESC")
    fun getAllItems(): Flow<List<MarketplaceItemEntity>>

    @Query("SELECT * FROM marketplace_items WHERE vendorEmail = :vendorEmail ORDER BY timestamp DESC")
    fun getItemsByVendor(vendorEmail: String): Flow<List<MarketplaceItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: MarketplaceItemEntity)

    @Delete
    suspend fun deleteItem(item: MarketplaceItemEntity)

    // --- ORDERS ---
    @Query("SELECT * FROM orders WHERE foodieEmail = :foodieEmail ORDER BY timestamp DESC")
    fun getOrdersForFoodie(foodieEmail: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE vendorEmail = :vendorEmail ORDER BY timestamp DESC")
    fun getOrdersForVendor(vendorEmail: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String)
}
