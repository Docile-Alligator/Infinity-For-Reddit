package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import ml.docilealligator.infinityforreddit.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemedTopAppBar(
    titleStringResId: Int,
    scrollBehavior: TopAppBarScrollBehavior,
    actions: @Composable RowScope.() -> Unit = {},
    onBack: () -> Unit
) {
    val appBarColor = lerp(
        start = Color(LocalAppTheme.current.colorPrimary),
        stop = Color.Transparent,
        fraction = scrollBehavior.state.collapsedFraction
    )

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = appBarColor,
            scrolledContainerColor = appBarColor,
            titleContentColor = Color(LocalAppTheme.current.toolbarPrimaryTextAndIconColor),
        ),
        title = {
            Text(stringResource(titleStringResId))
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                ToolbarIcon(
                    contentDescription = stringResource(R.string.action_back_content_description)
                )
            }
        },
        actions = actions,
        scrollBehavior = scrollBehavior
    )
}