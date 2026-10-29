package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import ml.docilealligator.infinityforreddit.R

@Composable
fun <T> CustomSpinner(
    modifier: Modifier = Modifier,
    isSpinnershownState: MutableState<Boolean>,
    options: List<T>,
    spinnerButtonText: String,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    optionName: (T) -> String,
    onOptionClick: (T) -> Unit
) {
    Box {
        Button(
            modifier = modifier,
            colors = ButtonDefaults.buttonColors().copy(
                containerColor = Color(LocalAppTheme.current.colorAccent),
                contentColor = Color(LocalAppTheme.current.fabIconColor)
            ),
            onClick = {
                isSpinnershownState.value = true
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    spinnerButtonText,
                    fontFamily = fontFamily,
                    fontSize = fontSize
                )

                Spacer(modifier = Modifier.width(4.dp))

                Image(
                    modifier = modifier,
                    painter = painterResource(if (isSpinnershownState.value) R.drawable.ic_baseline_arrow_drop_up_24dp else R.drawable.ic_baseline_arrow_drop_down_24dp),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(Color(LocalAppTheme.current.fabIconColor))
                )
            }
        }

        DropdownMenu(
            expanded = isSpinnershownState.value,
            containerColor = Color(LocalAppTheme.current.backgroundColor),
            onDismissRequest = { isSpinnershownState.value = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = {
                        PrimaryText(
                            optionName(option)
                        )
                    },
                    onClick = {
                        isSpinnershownState.value = false
                        onOptionClick(option)
                    }
                )
            }
        }
    }
}