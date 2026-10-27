package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.annotation.StringRes
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import ml.docilealligator.infinityforreddit.R

@Composable
fun CustomFilledButton(
    modifier: Modifier = Modifier,
    text: String,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    onclick: () -> Unit
) {
    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = Color(LocalAppTheme.current.colorPrimaryLightTheme),
            contentColor = Color(LocalAppTheme.current.buttonTextColor)
        ),
        onClick = onclick
    ) {
        Text(
            text,
            fontFamily = fontFamily,
            fontSize = fontSize
        )
    }
}

@Composable
fun CustomFilledButton(
    modifier: Modifier = Modifier,
    @StringRes stringResId: Int,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    onclick: () -> Unit
) {
    Button(
        modifier = modifier,
        colors = ButtonDefaults.buttonColors().copy(
            containerColor = Color(LocalAppTheme.current.colorPrimaryLightTheme),
            contentColor = Color(LocalAppTheme.current.buttonTextColor)
        ),
        onClick = onclick
    ) {
        Text(
            stringResource(stringResId),
            fontFamily = fontFamily,
            fontSize = fontSize
        )
    }
}

@Composable
fun CustomPositiveTextButton(
    modifier: Modifier = Modifier,
    @StringRes stringResId: Int,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    onclick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors().copy(
            contentColor = Color(LocalAppTheme.current.colorPrimaryLightTheme)
        ),
        onClick = onclick
    ) {
        Text(
            stringResource(stringResId),
            fontFamily = fontFamily,
            fontSize = fontSize
        )
    }
}

@Composable
fun CustomPositiveTextButton(
    modifier: Modifier = Modifier,
    text: String,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    onclick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors().copy(
            contentColor = Color(LocalAppTheme.current.colorPrimaryLightTheme)
        ),
        onClick = onclick
    ) {
        Text(
            text,
            fontFamily = fontFamily,
            fontSize = fontSize
        )
    }
}

@Composable
fun CustomNeutralTextButton(
    modifier: Modifier = Modifier,
    @StringRes stringResId: Int,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    onclick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors().copy(
            contentColor = Color(LocalAppTheme.current.primaryTextColor)
        ),
        onClick = onclick
    ) {
        Text(
            stringResource(stringResId),
            fontFamily = fontFamily,
            fontSize = fontSize
        )
    }
}

@Composable
fun CustomNegativeTextButton(
    modifier: Modifier = Modifier,
    @StringRes stringResId: Int,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    onclick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors().copy(
            contentColor = Color.Red
        ),
        onClick = onclick
    ) {
        Text(
            stringResource(stringResId),
            fontFamily = fontFamily,
            fontSize = fontSize
        )
    }
}

@Composable
fun CustomTextButton(
    modifier: Modifier = Modifier,
    text: String,
    textColor: Color = Color(LocalAppTheme.current.primaryTextColor),
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    onclick: () -> Unit
) {
    TextButton(
        modifier = modifier,
        colors = ButtonDefaults.textButtonColors().copy(
            contentColor = textColor
        ),
        onClick = onclick
    ) {
        Text(
            text,
            fontFamily = fontFamily,
            fontSize = fontSize
        )
    }
}

@Composable
fun SendMessageIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_send_rounded_24dp),
            contentDescription = stringResource(R.string.content_description_send_message),
            tint = Color(LocalAppTheme.current.sendMessageIconColor)
        )
    }
}