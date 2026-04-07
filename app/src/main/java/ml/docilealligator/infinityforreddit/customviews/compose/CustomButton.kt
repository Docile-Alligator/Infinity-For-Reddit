package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.annotation.StringRes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource

@Composable
fun CustomFilledButton(text: String, onclick: () -> Unit) {
    Button(
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = Color(LocalAppTheme.current.colorPrimaryLightTheme),
            contentColor = Color(LocalAppTheme.current.buttonTextColor)
        ),
        onClick = onclick
    ) {
        Text(text)
    }
}

@Composable
fun CustomFilledButton(@StringRes stringResId: Int, onclick: () -> Unit) {
    Button(
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = Color(LocalAppTheme.current.colorPrimaryLightTheme),
            contentColor = Color(LocalAppTheme.current.buttonTextColor)
        ),
        onClick = onclick
    ) {
        Text(stringResource(stringResId))
    }
}