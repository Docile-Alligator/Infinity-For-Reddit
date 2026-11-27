package ml.docilealligator.infinityforreddit.customviews.compose.shimmer

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier

internal val LocalShimmerLoading = staticCompositionLocalOf { true }

@Composable
fun ShimmerSurface(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalShimmerLoading provides isLoading) {
        Column(modifier) {
            content()
        }
    }
}
