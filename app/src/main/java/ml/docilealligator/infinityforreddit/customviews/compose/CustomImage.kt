package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ml.docilealligator.infinityforreddit.R

@Composable
fun PrimaryIcon(modifier: Modifier = Modifier, drawableId: Int, contentDescription: String) {
    Image(
        modifier = modifier,
        painter = painterResource(drawableId),
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(Color(LocalAppTheme.current.primaryIconColor))
    )
}

@Composable
fun ToolbarIcon(modifier: Modifier = Modifier, drawableId: Int = R.drawable.ic_arrow_back_24dp, contentDescription: String) {
    Image(
        modifier = modifier.size(24.dp),
        painter = painterResource(drawableId),
        contentDescription = contentDescription,
        colorFilter = ColorFilter.tint(Color(LocalAppTheme.current.toolbarPrimaryTextAndIconColor))
    )
}