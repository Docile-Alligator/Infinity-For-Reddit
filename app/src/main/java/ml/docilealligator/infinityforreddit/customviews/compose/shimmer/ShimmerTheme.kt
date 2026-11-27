package ml.docilealligator.infinityforreddit.customviews.compose.shimmer

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class ShimmerThemeValues(
    val shimmerColor: Color,
    val backgroundColor: Color,
    val durationMillis: Int
)

val LocalShimmerTheme = staticCompositionLocalOf {
    ShimmerThemeValues(
        shimmerColor = Color.White.copy(alpha = 0.35f),
        backgroundColor = Color.LightGray.copy(alpha = 0.35f),
        durationMillis = 1400
    )
}

@Composable
fun ProvideShimmerTheme(
    lightShimmerColor: Color = Color.White.copy(alpha = 0.35f),
    lightBackgroundColor: Color = Color.LightGray.copy(alpha = 0.35f),
    darkShimmerColor: Color = Color.White.copy(alpha = 0.20f),
    darkBackgroundColor: Color = Color.DarkGray.copy(alpha = 0.20f),
    durationMillis: Int = 1400,
    content: @Composable () -> Unit
) {
    val dark = isSystemInDarkTheme()

    val theme = if (dark) {
        ShimmerThemeValues(
            shimmerColor = darkShimmerColor,
            backgroundColor = darkBackgroundColor,
            durationMillis = durationMillis
        )
    } else {
        ShimmerThemeValues(
            shimmerColor = lightShimmerColor,
            backgroundColor = lightBackgroundColor,
            durationMillis = durationMillis
        )
    }

    CompositionLocalProvider(LocalShimmerTheme provides theme, content = content)
}