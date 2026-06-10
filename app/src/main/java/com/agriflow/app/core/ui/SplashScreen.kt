package com.agriflow.app.core.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agriflow.app.R
import kotlinx.coroutines.delay

@Composable
fun SplashRoute(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // LaunchedEffect runs this delay once when the splash enters composition.
    // Change SPLASH_DURATION_MILLIS below if you want the splash to stay longer or shorter.
    LaunchedEffect(Unit) {
        delay(SPLASH_DURATION_MILLIS)
        onSplashFinished()
    }

    SplashScreen(modifier = modifier)
}

@Composable
private fun SplashScreen(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBackground)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.agriflow_logo),
            contentDescription = "Agriflow logo",
            contentScale = ContentScale.Fit,
            // widthIn keeps the logo from becoming comically large on tablets.
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 360.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Agriflow",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = SplashGreen
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Agricultural trade, made secure.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private const val SPLASH_DURATION_MILLIS = 1_500L

// Keeping these colors local makes this screen easy to tune without affecting the app theme.
private val SplashBackground = Color(0xFFF7FAF5)
private val SplashGreen = Color(0xFF14532D)
