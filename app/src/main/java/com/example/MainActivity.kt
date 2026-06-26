package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.data.AppDatabase
import com.example.data.MarketplaceRepository
import com.example.ui.MarketplaceApp
import com.example.ui.MarketplaceViewModel
import com.example.ui.MarketplaceViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize Room Database & Repository
        val database = AppDatabase.getDatabase(this)
        val repository = MarketplaceRepository(database.marketplaceDao())

        // Create ViewModel
        val viewModel: MarketplaceViewModel by viewModels {
            MarketplaceViewModelFactory(repository)
        }

        setContent {
            MyApplicationTheme {
                MarketplaceApp(viewModel = viewModel)
            }
        }
    }
}

