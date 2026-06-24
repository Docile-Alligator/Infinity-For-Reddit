package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit

@Composable
fun PrimaryText(
    stringResourceId: Int,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    textAlign: TextAlign? = null
) {
    Text(
        stringResource(stringResourceId),
        color = Color(LocalAppTheme.current.primaryTextColor),
        fontFamily = fontFamily,
        fontSize = fontSize,
        textAlign = textAlign
    )
}

@Composable
fun PrimaryText(
    text: String,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    textAlign: TextAlign? = null
) {
    Text(
        text,
        color = Color(LocalAppTheme.current.primaryTextColor),
        fontFamily = fontFamily,
        fontSize = fontSize,
        textAlign = textAlign
    )
}

@Composable
fun SecondaryText(
    stringResourceId: Int,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    textAlign: TextAlign? = null
) {
    Text(
        stringResource(stringResourceId),
        color = Color(LocalAppTheme.current.secondaryTextColor),
        fontFamily = fontFamily,
        fontSize = fontSize,
        textAlign = textAlign
    )
}

@Composable
fun SecondaryText(
    text: String,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    textAlign: TextAlign? = null
) {
    Text(
        text,
        color = Color(LocalAppTheme.current.secondaryTextColor),
        fontFamily = fontFamily,
        fontSize = fontSize,
        textAlign = textAlign
    )
}

@Composable
fun SecondaryText(
    text: AnnotatedString,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    textAlign: TextAlign? = null
) {
    Text(
        text,
        color = Color(LocalAppTheme.current.secondaryTextColor),
        fontFamily = fontFamily,
        fontSize = fontSize,
        textAlign = textAlign
    )
}