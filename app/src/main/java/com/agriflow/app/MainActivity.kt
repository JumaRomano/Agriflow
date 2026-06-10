package com.agriflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.agriflow.app.core.navigation.AgriflowNavHost
import com.agriflow.app.ui.theme.AgriflowTheme // Your generated theme package might differ slightly
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint // NEVER FORGET THIS! It tells Hilt to allow dependency injection here.
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // This applies your Material 3 colors and typography
            AgriflowTheme {
                // A surface container uses the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // We 'remember' the navController so it survives recompositions (screen rotations)
                    val navController = rememberNavController()

                    // We drop our NavHost into the UI under app/core/presentation/navigation/NavHost
                    AgriflowNavHost(navController = navController)

                }
            }
        }
    }
}