package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MarketplaceRepository(private val dao: MarketplaceDao) {

    // --- USERS ---
    suspend fun getUserByEmail(email: String): UserEntity? = withContext(Dispatchers.IO) {
        dao.getUserByEmail(email)
    }

    suspend fun registerUser(user: UserEntity) = withContext(Dispatchers.IO) {
        dao.insertUser(user)
    }

    // --- MARKETPLACE ITEMS ---
    fun getAllItems(): Flow<List<MarketplaceItemEntity>> = dao.getAllItems()

    fun getItemsByVendor(vendorEmail: String): Flow<List<MarketplaceItemEntity>> {
        return dao.getItemsByVendor(vendorEmail)
    }

    suspend fun addMarketplaceItem(item: MarketplaceItemEntity) = withContext(Dispatchers.IO) {
        dao.insertItem(item)
    }

    suspend fun deleteMarketplaceItem(item: MarketplaceItemEntity) = withContext(Dispatchers.IO) {
        dao.deleteItem(item)
    }

    // --- ORDERS ---
    fun getOrdersForFoodie(foodieEmail: String): Flow<List<OrderEntity>> {
        return dao.getOrdersForFoodie(foodieEmail)
    }

    fun getOrdersForVendor(vendorEmail: String): Flow<List<OrderEntity>> {
        return dao.getOrdersForVendor(vendorEmail)
    }

    suspend fun placeOrder(order: OrderEntity) = withContext(Dispatchers.IO) {
        dao.insertOrder(order)
    }

    suspend fun updateOrderStatus(orderId: Int, status: String) = withContext(Dispatchers.IO) {
        dao.updateOrderStatus(orderId, status)
    }

    // --- PRE-POPULATE DATA ---
    suspend fun prePopulateIfEmpty() = withContext(Dispatchers.IO) {
        // Pre-populate users
        val existingFoodie = dao.getUserByEmail("foodie@gmail.com")
        if (existingFoodie == null) {
            dao.insertUser(
                UserEntity(
                    email = "foodie@gmail.com",
                    passwordHash = "foodie123", // Simple plain-text comparison for demo purposes
                    role = "FOODIE",
                    displayName = "Alex Foodie",
                    isGoogleUser = false
                )
            )
        }

        val existingVendor = dao.getUserByEmail("vendor@gmail.com")
        if (existingVendor == null) {
            dao.insertUser(
                UserEntity(
                    email = "vendor@gmail.com",
                    passwordHash = "vendor123",
                    role = "VENDOR",
                    displayName = "Spiced Bistro (Vendor)",
                    isGoogleUser = false
                )
            )
        }

        // Pre-populate items if database is empty
        val allItems = dao.getAllItems().first()
        if (allItems.isEmpty()) {
            val sampleItems = listOf(
                MarketplaceItemEntity(
                    title = "Artisanal Truffle Burger",
                    description = "Double-smashed grass-fed beef patty with melted white cheddar, truffle aioli, wild mushrooms, caramelized onions, and fresh arugula on a toasted brioche bun. Served with sea salt fries.",
                    price = 16.99,
                    vendorEmail = "vendor@gmail.com",
                    vendorName = "Spiced Bistro",
                    category = "Burger"
                ),
                MarketplaceItemEntity(
                    title = "Stone-Fired Neapolitan Pizza",
                    description = "Traditional Neapolitan thin crust topped with fresh organic crushed San Marzano tomatoes, fresh mozzarella di bufala, extra virgin olive oil, and sweet basil leaves.",
                    price = 14.50,
                    vendorEmail = "pizza@vendor.com",
                    vendorName = "Luigi's Woodfire",
                    category = "Pizza"
                ),
                MarketplaceItemEntity(
                    title = "Golden Salted Caramel Cheesecake",
                    description = "Decadent cream cheese filling on a crumbly graham cracker crust, topped with handmade salted caramel glaze, dark chocolate drizzle, and fresh pecans.",
                    price = 7.25,
                    vendorEmail = "bakery@vendor.com",
                    vendorName = "Sweet Treats",
                    category = "Dessert"
                ),
                MarketplaceItemEntity(
                    title = "Iced Madagascar Vanilla Latte",
                    description = "Espresso pulled from freshly roasted organic beans blended with gourmet Madagascar vanilla syrup, creamy chilled oat milk, and ice.",
                    price = 5.50,
                    vendorEmail = "vendor@gmail.com",
                    vendorName = "Spiced Bistro",
                    category = "Drinks"
                ),
                MarketplaceItemEntity(
                    title = "Spicy Tonkotsu Ramen Bowls",
                    description = "Rich 16-hour pork bone broth served with tender chashu pork belly, soft-boiled marinated egg, black garlic oil, bamboo shoots, and green scallions.",
                    price = 15.99,
                    vendorEmail = "ramen@vendor.com",
                    vendorName = "Ichiran Tokyo",
                    category = "Asian"
                )
            )

            for (item in sampleItems) {
                dao.insertItem(item)
            }
        }
    }
}
