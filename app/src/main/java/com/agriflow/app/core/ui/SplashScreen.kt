/**
 * Jetpack Compose UI screen components for the Splash screen.
 */
package com.agriflow.app.core.ui

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
    var startAnim by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnim = true
    }

    // 1. Spring physics overshoot for the Logo scale landing
    val logoScale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.75f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "LogoScale"
    )
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "LogoAlpha"
    )

    // 2. Infinite radar wave rings expanding from behind the logo
    val infiniteTransition = rememberInfiniteTransition(label = "SplashRadarTransition")

    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale1"
    )
    val pulseAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha1"
    )

    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseOutQuad, delayMillis = 1100),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseScale2"
    )
    val pulseAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseOutQuad, delayMillis = 1100),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha2"
    )

    // 3. Staggered fade & slide up for the brand name Title
    val titleAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 150),
        label = "TitleAlpha"
    )
    val titleOffsetY by animateFloatAsState(
        targetValue = if (startAnim) 0f else 16f,
        animationSpec = tween(durationMillis = 400, delayMillis = 150, easing = EaseOutCubic),
        label = "TitleOffsetY"
    )

    // 4. Staggered fade & slide up for the brand slogan Subtitle
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 300),
        label = "SubtitleAlpha"
    )
    val subtitleOffsetY by animateFloatAsState(
        targetValue = if (startAnim) 0f else 12f,
        animationSpec = tween(durationMillis = 500, delayMillis = 300, easing = EaseOutCubic),
        label = "SubtitleOffsetY"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SplashBackground)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(240.dp)
        ) {
            // Pulse wave 1 (only show once logo has started to land)
            if (startAnim) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = SplashGreen,
                        radius = (size.minDimension / 2.2f) * pulseScale1,
                        alpha = pulseAlpha1,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                // Pulse wave 2
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = SplashGreen,
                        radius = (size.minDimension / 2.2f) * pulseScale2,
                        alpha = pulseAlpha2,
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Branded "agri" logo Image
            Image(
                painter = painterResource(id = R.drawable.agriflow_logo),
                contentDescription = "Agriflow logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 200.dp)
                    .graphicsLayer(
                        scaleX = logoScale,
                        scaleY = logoScale,
                        alpha = logoAlpha
                    )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Agriflow",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = SplashGreen,
            modifier = Modifier.graphicsLayer(
                alpha = titleAlpha,
                translationY = titleOffsetY
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Agricultural trade, made secure.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer(
                alpha = subtitleAlpha,
                translationY = subtitleOffsetY
            )
        )
    }
}

private const val SPLASH_DURATION_MILLIS = 1_800L

// Keeping these colors local makes this screen easy to tune without affecting the app theme.
private val SplashBackground = Color(0xFFF7FAF5)
private val SplashGreen = Color(0xFF14532D)
