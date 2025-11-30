package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign

@Composable
fun PrimaryText(stringResourceId: Int, textAlign: TextAlign? = null) {
    Text(
        stringResource(stringResourceId),
        color = Color(LocalAppTheme.current.primaryTextColor),
        textAlign = textAlign
    )
}

@Composable
fun PrimaryText(text: String, textAlign: TextAlign? = null) {
    Text(
        text,
        color = Color(LocalAppTheme.current.primaryTextColor),
        textAlign = textAlign
    )
}