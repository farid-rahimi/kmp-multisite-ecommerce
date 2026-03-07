package com.solutionium.sharedui.checkout

import com.solutionium.sharedui.resources.*

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun PlacingOrderScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // A "nice" loading indicator, e.g., Lottie animation or a more complex Compose animation
            //CircularProgressIndicator(modifier = Modifier.size(64.dp))
            PulsingLoadingIndicator()
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = stringResource(Res.string.placing_order_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(Res.string.placing_order_content),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * A custom loading indicator with three dots that pulse in sequence.
 * This animation is self-contained and reusable.
 */
@Composable
private fun PulsingLoadingIndicator(
    modifier: Modifier = Modifier
) {
    val dotCount = 3
    val animationDelay = 200 // Delay in ms between each dot's animation start

    // Create an Animatable for each dot
    val dots = List(dotCount) {
        remember { Animatable(0.5f) } // Start with a smaller scale
    }

    // Launch an effect for each dot to start its animation
    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * animationDelay.toLong()) // Stagger the start of each animation

            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600),
                    repeatMode = RepeatMode.Reverse // Pulse in and out
                )
            )
        }
    }

    // The Row that lays out the dots
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        dots.forEach { animatable ->
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .scale(animatable.value) // The scale is controlled by the animation
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}