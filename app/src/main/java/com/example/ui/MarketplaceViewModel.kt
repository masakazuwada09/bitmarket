package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MarketplaceItemEntity
import com.example.data.MarketplaceRepository
import com.example.data.OrderEntity
import com.example.data.UserEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Splash : Screen()
    object Auth : Screen()
    data class FoodieDashboard(val user: UserEntity) : Screen()
    data class VendorDashboard(val user: UserEntity) : Screen()
}

class MarketplaceViewModel(private val repository: MarketplaceRepository) : ViewModel() {

    // --- SCREEN STATE ---
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // --- FORM STATES ---
    var emailInput = MutableStateFlow("")
    var passwordInput = MutableStateFlow("")
    var nameInput = MutableStateFlow("")
    var selectedRole = MutableStateFlow("FOODIE") // "FOODIE" or "VENDOR"
    var isSignUpMode = MutableStateFlow(false) // False = Login, True = Sign Up

    // --- ERROR STATES ---
    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // --- GOOGLE SIGN IN SIMULATION ---
    private val _isGoogleSignInOpen = MutableStateFlow(false)
    val isGoogleSignInOpen: StateFlow<Boolean> = _isGoogleSignInOpen.asStateFlow()

    private val _googleEmailInput = MutableStateFlow("")
    val googleEmailInput: StateFlow<String> = _googleEmailInput.asStateFlow()

    // --- ACTIVE SESSION ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // --- DATA LISTS ---
    val allItems: StateFlow<List<MarketplaceItemEntity>> = repository.getAllItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _orders = MutableStateFlow<List<OrderEntity>>(emptyList())
    val orders: StateFlow<List<OrderEntity>> = _orders.asStateFlow()

    // --- CHECKOUT FLOW ---
    private val _selectedItemForOrder = MutableStateFlow<MarketplaceItemEntity?>(null)
    val selectedItemForOrder: StateFlow<MarketplaceItemEntity?> = _selectedItemForOrder.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prePopulateIfEmpty()
        }
    }

    fun setGoogleEmail(email: String) {
        _googleEmailInput.value = email
    }

    fun openGoogleSignIn() {
        _isGoogleSignInOpen.value = true
        _authError.value = null
    }

    fun closeGoogleSignIn() {
        _isGoogleSignInOpen.value = false
    }

    fun setScreen(screen: Screen) {
        _currentScreen.value = screen
        _authError.value = null
        if (screen is Screen.FoodieDashboard) {
            _currentUser.value = screen.user
            observeOrdersForFoodie(screen.user.email)
        } else if (screen is Screen.VendorDashboard) {
            _currentUser.value = screen.user
            observeOrdersForVendor(screen.user.email)
        } else {
            _currentUser.value = null
        }
    }

    private fun observeOrdersForFoodie(email: String) {
        viewModelScope.launch {
            repository.getOrdersForFoodie(email).collect {
                _orders.value = it
            }
        }
    }

    private fun observeOrdersForVendor(email: String) {
        viewModelScope.launch {
            repository.getOrdersForVendor(email).collect {
                _orders.value = it
            }
        }
    }

    // --- AUTHENTICATION ACTIONS ---

    fun handleEmailAuth() {
        _authError.value = null
        val email = emailInput.value.trim()
        val password = passwordInput.value

        // Common Validations
        if (email.isEmpty() || password.isEmpty()) {
            _authError.value = "Please fill in all fields."
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authError.value = "Please enter a valid email address."
            return
        }

        if (isSignUpMode.value) {
            // Sign Up Flow
            val displayName = nameInput.value.trim()
            if (displayName.isEmpty()) {
                _authError.value = "Please enter your name."
                return
            }

            if (password.length < 6) {
                _authError.value = "Password must be at least 6 characters long."
                return
            }

            viewModelScope.launch {
                val existingUser = repository.getUserByEmail(email)
                if (existingUser != null) {
                    _authError.value = "This email is already registered."
                } else {
                    val newUser = UserEntity(
                        email = email,
                        passwordHash = password, // simple demo comparison
                        role = selectedRole.value,
                        displayName = displayName,
                        isGoogleUser = false
                    )
                    repository.registerUser(newUser)
                    // Navigate directly
                    if (newUser.role == "FOODIE") {
                        setScreen(Screen.FoodieDashboard(newUser))
                    } else {
                        setScreen(Screen.VendorDashboard(newUser))
                    }
                }
            }
        } else {
            // Login Flow
            viewModelScope.launch {
                val user = repository.getUserByEmail(email)
                if (user == null) {
                    _authError.value = "This email is not registered on BiteMarket. Please sign up first."
                } else {
                    if (user.passwordHash != password) {
                        _authError.value = "Incorrect password. Please try again."
                    } else {
                        // Success!
                        if (user.role == "FOODIE") {
                            setScreen(Screen.FoodieDashboard(user))
                        } else {
                            setScreen(Screen.VendorDashboard(user))
                        }
                    }
                }
            }
        }
    }

    fun handleGoogleSignIn() {
        val email = _googleEmailInput.value.trim()
        _authError.value = null

        if (email.isEmpty()) {
            _authError.value = "Google email cannot be empty."
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authError.value = "Please enter a valid email address."
            return
        }

        // Error check: "non-Gmail address"
        if (!email.endsWith("@gmail.com")) {
            _authError.value = "Google sign-in is restricted to Gmail accounts."
            return
        }

        closeGoogleSignIn()

        // Google Authentication simulation:
        // Automatically check if user exists. If yes, log them in. If not, create them with Google role-based registration!
        viewModelScope.launch {
            val existingUser = repository.getUserByEmail(email)
            if (existingUser != null) {
                // Pre-existing user logs in
                if (existingUser.role == "FOODIE") {
                    setScreen(Screen.FoodieDashboard(existingUser))
                } else {
                    setScreen(Screen.VendorDashboard(existingUser))
                }
            } else {
                // New Google user registration. Default to Foodie or prompt Vendor? Let's default to FOODIE for demo Google Sign In,
                // or use the current form's role selection so the user can test Google Auth as either Foodie or Vendor!
                val role = selectedRole.value
                val newUser = UserEntity(
                    email = email,
                    passwordHash = "google_oauth_token",
                    role = role,
                    displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() } + " (Google)",
                    isGoogleUser = true
                )
                repository.registerUser(newUser)
                if (newUser.role == "FOODIE") {
                    setScreen(Screen.FoodieDashboard(newUser))
                } else {
                    setScreen(Screen.VendorDashboard(newUser))
                }
            }
        }
    }

    // --- MARKETPLACE ACTIONS ---

    fun openCheckout(item: MarketplaceItemEntity) {
        _selectedItemForOrder.value = item
    }

    fun closeCheckout() {
        _selectedItemForOrder.value = null
    }

    fun checkoutAndPlaceOrder() {
        val item = _selectedItemForOrder.value ?: return
        val user = _currentUser.value ?: return

        viewModelScope.launch {
            val order = OrderEntity(
                itemId = item.id,
                itemTitle = item.title,
                itemPrice = item.price,
                foodieEmail = user.email,
                vendorEmail = item.vendorEmail,
                status = "PENDING"
            )
            repository.placeOrder(order)
            closeCheckout()
        }
    }

    fun addNewDish(title: String, category: String, description: String, priceStr: String) {
        val user = _currentUser.value ?: return
        val price = priceStr.toDoubleOrNull() ?: 0.0

        viewModelScope.launch {
            val newItem = MarketplaceItemEntity(
                title = title,
                description = description,
                price = price,
                vendorEmail = user.email,
                vendorName = user.displayName,
                category = category
            )
            repository.addMarketplaceItem(newItem)
        }
    }

    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }

    fun logout() {
        emailInput.value = ""
        passwordInput.value = ""
        nameInput.value = ""
        _authError.value = null
        setScreen(Screen.Auth)
    }

    fun fillCredentials(email: String, pwhash: String, role: String) {
        emailInput.value = email
        passwordInput.value = pwhash
        selectedRole.value = role
        isSignUpMode.value = false
        _authError.value = null
    }
}

class MarketplaceViewModelFactory(private val repository: MarketplaceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MarketplaceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MarketplaceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
