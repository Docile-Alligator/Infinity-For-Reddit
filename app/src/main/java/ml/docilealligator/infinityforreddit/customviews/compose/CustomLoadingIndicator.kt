package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CustomLoadingIndicator() {
    LoadingIndicator(
        color = Color(LocalAppTheme.current.colorAccent)
    )
}