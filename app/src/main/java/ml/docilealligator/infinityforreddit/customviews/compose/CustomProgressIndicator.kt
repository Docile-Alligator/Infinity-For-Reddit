package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomCircularWavyProgressIndicator(
    modifier: Modifier = Modifier
) {
    CircularWavyProgressIndicator(
        modifier = modifier,
        color = Color(LocalAppTheme.current.colorAccent),
        trackColor = Color(LocalAppTheme.current.buttonTextColor)
    )
}