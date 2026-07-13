package com.agriflow.app.features.auth.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A snappy, hardware-accelerated entrance animation for UI components.
 * Staggers items sequentially based on the provided [index] to create a fluid cascading effect.
 */
@Composable
fun StaggeredEntrance(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animatableAlpha = remember { Animatable(0f) }
    val animatableOffsetY = remember { Animatable(16f) } // Snappy 16dp translation for a modern feel

    LaunchedEffect(Unit) {
        // snappy 40ms interval per index
        delay(index * 40L)
        launch {
            animatableAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 300, easing = EaseOutCubic)
            )
        }
        launch {
            animatableOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 300, easing = EaseOutCubic)
            )
        }
    }

    Box(
        modifier = modifier.graphicsLayer(
            alpha = animatableAlpha.value,
            translationY = animatableOffsetY.value
        )
    ) {
        content()
    }
}

/**
 * A highly-polished, brand-aligned entrance animation for welcome headers.
 * Combines scale overshoot spring physics and letter-spacing contraction for the title,
 * and a staggered fade + slide up for the subtitle.
 */
@Composable
fun LoginWelcomeHeaderAnimation(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    var startAnim by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnim = true
    }

    // Overshoot scale spring animation for the Title
    val scale by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0.85f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "HeaderScale"
    )

    // Fade-in animation for the Title
    val titleAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "TitleAlpha"
    )

    // Letter-spacing contraction animation for the Title: starts expanded and shrinks to normal (0.sp)
    val letterSpacing by animateFloatAsState(
        targetValue = if (startAnim) 0f else 5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessVeryLow
        ),
        label = "LetterSpacing"
    )

    // Subtitle staggered slide & fade-in (starts shortly after title)
    val subtitleAlpha by animateFloatAsState(
        targetValue = if (startAnim) 1f else 0f,
        animationSpec = tween(durationMillis = 500, delayMillis = 180, easing = EaseOutCubic),
        label = "SubtitleAlpha"
    )

    val subtitleOffsetY by animateFloatAsState(
        targetValue = if (startAnim) 0f else 12f,
        animationSpec = tween(durationMillis = 500, delayMillis = 180, easing = EaseOutCubic),
        label = "SubtitleOffsetY"
    )

    Column(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            letterSpacing = letterSpacing.sp,
            modifier = Modifier.graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                alpha = titleAlpha
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.graphicsLayer(
                alpha = subtitleAlpha,
                translationY = subtitleOffsetY
            )
        )
    }
}

/**
 * Renders subtle, slowly floating/rotating background ornaments using existing theme colors.
 * Increased visibility: alphas changed from 0.05f/0.04f to 0.12f/0.10f to make them clearer.
 * Strict compliance: no gradient colors or custom colors are introduced.
 */
@Composable
fun AuthBackgroundOrnaments(
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val infiniteTransition = rememberInfiniteTransition(label = "AuthOrnamentsTransition")

    // Slow drifting float values
    val driftY1 by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY1"
    )

    val driftY2 by infiniteTransition.animateFloat(
        initialValue = 15f,
        targetValue = -15f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driftY2"
    )

    val rotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation1"
    )

    val rotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(50000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation2"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Ornament 1: Top-Right Floating Circle with concentric ring (increased alpha to 0.12f)
        Canvas(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = (-50).dp)
                .graphicsLayer(
                    translationY = driftY1,
                    rotationZ = rotation1,
                    alpha = 0.12f
                )
        ) {
            // Draw an outer ring
            drawCircle(
                color = primaryColor,
                radius = size.minDimension / 2f,
                style = Stroke(width = 2.dp.toPx())
            )
            // Draw an inner ring
            drawCircle(
                color = primaryColor,
                radius = size.minDimension / 2.8f,
                style = Stroke(width = 1.dp.toPx())
            )
        }

        // Ornament 2: Bottom-Left Floating Leaf Outline (increased alpha to 0.10f)
        Canvas(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-100).dp, y = 100.dp)
                .graphicsLayer(
                    translationY = driftY2,
                    rotationZ = rotation2,
                    alpha = 0.10f
                )
        ) {
            val width = size.width
            val height = size.height
            val leafPath = Path().apply {
                moveTo(width * 0.5f, 0f)
                cubicTo(width * 0.9f, height * 0.2f, width, height * 0.6f, width * 0.5f, height)
                cubicTo(0f, height * 0.6f, width * 0.1f, height * 0.2f, width * 0.5f, 0f)
                // Center stem
                moveTo(width * 0.5f, 0f)
                lineTo(width * 0.5f, height)
            }
            drawPath(
                path = leafPath,
                color = secondaryColor,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }
}
