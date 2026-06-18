/**
 * Represents the class [MainActivity] providing core functionality within the application.
 */
package com.agriflow.app

import android.os.Build
import android.Manifest
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


import androidx.lifecycle.lifecycleScope
import com.google.firebase.messaging.FirebaseMessaging
import com.agriflow.app.core.security.TokenRepository
import com.agriflow.app.features.notifications.NotificationsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint // NEVER FORGET THIS! It tells Hilt to allow dependency injection here.
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenRepository: TokenRepository

    @Inject
    lateinit var notificationsRepository: NotificationsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermission()
        
        lifecycleScope.launch {
            tokenRepository.getUserFlow().collect { user ->
                if (user != null) {
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val token = task.result
                            lifecycleScope.launch(Dispatchers.IO) {
                                notificationsRepository.registerDeviceToken(token, "android")
                            }
                        }
                    }
                }
            }
        }

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

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 101)
            }
        }
    }
}