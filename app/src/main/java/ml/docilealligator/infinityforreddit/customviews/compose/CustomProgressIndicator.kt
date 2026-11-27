package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ml.docilealligator.infinityforreddit.utils.deriveContrastingColor

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomCircularWavyProgressIndicator(
    modifier: Modifier = Modifier
) {
    val colorAccent = LocalAppTheme.current.colorAccent
    val trackColor = remember { deriveContrastingColor(colorAccent) }

    CircularWavyProgressIndicator(
        modifier = modifier,
        color = Color(LocalAppTheme.current.colorAccent),
        trackColor = Color(trackColor)
    )
}