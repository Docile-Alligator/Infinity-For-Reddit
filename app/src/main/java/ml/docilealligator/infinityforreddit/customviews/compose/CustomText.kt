package ml.docilealligator.infinityforreddit.customviews.compose

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Composable
fun PrimaryText(
    stringResourceId: Int,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    textAlign: TextAlign? = null
) {
    Text(
        stringResource(stringResourceId),
        modifier = modifier,
        color = Color(LocalAppTheme.current.primaryTextColor),
        fontFamily = fontFamily,
        fontSize = fontSize,
        textAlign = textAlign
    )
}

@Composable
fun PrimaryText(
    text: String,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = Color(LocalAppTheme.current.primaryTextColor),
        fontFamily = fontFamily,
        fontSize = fontSize,
        textAlign = textAlign,
        fontWeight = fontWeight
    )
}

@Composable
fun SecondaryText(
    stringResourceId: Int,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    textAlign: TextAlign? = null
) {
    Text(
        stringResource(stringResourceId),
        modifier = modifier,
        color = Color(LocalAppTheme.current.secondaryTextColor),
        fontFamily = fontFamily,
        fontSize = fontSize,
        textAlign = textAlign
    )
}

@Composable
fun SecondaryText(
    text: String,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    textAlign: TextAlign? = null
) {
    Text(
        text,
        modifier = modifier,
        color = Color(LocalAppTheme.current.secondaryTextColor),
        fontFamily = fontFamily,
        fontSize = fontSize,
        textAlign = textAlign
    )
}

@Composable
fun SecondaryText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    fontFamily: FontFamily? = LocalTypography.current.fontFamily,
    fontSize: TextUnit = LocalTypography.current.fontSize.default,
    textAlign: TextAlign? = null
) {
    Text(
        text,
        modifier = modifier,
        color = Color(LocalAppTheme.current.secondaryTextColor),
        fontFamily = fontFamily,
        fontSize = fontSize,
        textAlign = textAlign
    )
}