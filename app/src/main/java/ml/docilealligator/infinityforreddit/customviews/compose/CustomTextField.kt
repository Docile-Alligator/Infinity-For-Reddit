package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun CustomTextField(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    placeholder: String,
    lineLimits: TextFieldLineLimits = TextFieldLineLimits.Default
) {
    OutlinedTextField(
        modifier = modifier,
        state = state,
        placeholder = {
            Text(
                text = placeholder,
                color = Color(LocalAppTheme.current.secondaryTextColor)
            )
        },
        lineLimits = lineLimits,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color(LocalAppTheme.current.primaryTextColor),
            unfocusedTextColor = Color(LocalAppTheme.current.primaryTextColor),
            focusedBorderColor = Color(LocalAppTheme.current.primaryTextColor),
            unfocusedBorderColor = Color(LocalAppTheme.current.secondaryTextColor),
            cursorColor = Color(LocalAppTheme.current.colorPrimary)
        )
    )
}