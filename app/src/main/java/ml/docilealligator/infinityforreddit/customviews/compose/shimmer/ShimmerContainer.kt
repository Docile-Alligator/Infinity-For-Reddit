package ml.docilealligator.infinityforreddit.customviews.compose.shimmer

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun ShimmerContainer(
    modifier: Modifier = Modifier,
    isLoading: Boolean = LocalShimmerLoading.current,
    content: @Composable () -> Unit
) {
    val theme = LocalShimmerTheme.current
    val transition = if (isLoading) rememberInfiniteTransition() else null

    val shimmerX by (transition?.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(theme.durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    ) ?: remember { mutableStateOf(0f) })

    val brush = remember(shimmerX, isLoading) {
        if (isLoading) {
            Brush.linearGradient(
                colors = listOf(theme.backgroundColor, theme.shimmerColor, theme.backgroundColor),
                start = Offset.Zero,
                end = Offset(shimmerX * 600f, shimmerX * 600f)
            )
        } else null
    }

    Box(
        modifier = modifier
            .then(
                if (isLoading) Modifier
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
                    .drawWithContent {
                        drawContent()
                        brush?.let {
                            drawRect(
                                brush = it,
                                blendMode = BlendMode.SrcAtop
                            )
                        }
                    }
                else Modifier
            )
    ) {
        content()
    }
}