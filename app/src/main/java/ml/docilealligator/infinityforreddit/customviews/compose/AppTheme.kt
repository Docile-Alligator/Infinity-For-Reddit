package ml.docilealligator.infinityforreddit.customviews.compose

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.giphy.sdk.analytics.GiphyPingbacks.context
import kotlinx.coroutines.flow.onEach
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customtheme.LocalCustomThemeRepository
import ml.docilealligator.infinityforreddit.font.FontStyle
import ml.docilealligator.infinityforreddit.utils.CustomThemeSharedPreferencesUtils
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils

val LocalAppTheme = staticCompositionLocalOf<CustomTheme> {
    error("No default theme values")
}

val LocalTypography = staticCompositionLocalOf<Typography> {
    error("No default typography values")
}

class Typography(
    val fontFamily: FontFamily?,
    val titleFontFamily: FontFamily?,
    val contentFontFamily: FontFamily?,
    val fontSize: FontSize,
    val titleFontSize: TitleFontSize,
    val contentFontSize: ContentFontSize
)

class FontSize(
    val default: TextUnit,
    val size10: TextUnit,
    val size12: TextUnit,
    val size16: TextUnit,
    val size18: TextUnit,
    val size20: TextUnit
)

class TitleFontSize(
    val default: TextUnit,
    val size12: TextUnit,
    val size16: TextUnit,
    val size18: TextUnit,
    val size20: TextUnit
)

class ContentFontSize(
    val default: TextUnit,
    val size12: TextUnit,
    val size16: TextUnit,
    val size18: TextUnit,
    val size20: TextUnit
)

@Composable
fun AppTheme(themeType: Int, sharedPreferences: SharedPreferences, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val localCustomThemeRepository = LocalCustomThemeRepository(((context.applicationContext) as Infinity).mRedditDataRoomDatabase)
    var themeLoaded by remember { mutableStateOf(false) }
    val typography by remember {
        mutableStateOf(getTypography(context, sharedPreferences))
    }

    val currentThemeFlow = remember {
        when(themeType) {
            CustomThemeSharedPreferencesUtils.LIGHT -> localCustomThemeRepository.currentLightCustomThemeFlow
            CustomThemeSharedPreferencesUtils.DARK -> localCustomThemeRepository.currentDarkCustomThemeFlow
            CustomThemeSharedPreferencesUtils.AMOLED -> localCustomThemeRepository.currentAmoledCustomThemeFlow
            else -> localCustomThemeRepository.currentLightCustomThemeFlow
        }.onEach {
            themeLoaded = true
        }
    }

    val customTheme by currentThemeFlow.collectAsState(null)

    if (themeLoaded) {
        CompositionLocalProvider(
            LocalAppTheme provides (customTheme ?: getDefaultTheme(context, themeType)),
            LocalTypography provides typography
        ) {
            MaterialTheme {
                content()
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.05f)))
    }
}

private fun getDefaultTheme(context: Context, themeType: Int): CustomTheme {
    return when(themeType) {
        CustomThemeSharedPreferencesUtils.LIGHT -> CustomThemeWrapper.getIndigo(context)
        CustomThemeSharedPreferencesUtils.DARK -> CustomThemeWrapper.getIndigoDark(context)
        CustomThemeSharedPreferencesUtils.AMOLED -> CustomThemeWrapper.getIndigoAmoled(context)
        else -> CustomThemeWrapper.getIndigo(context)
    }
}

private fun getTypography(context: Context, sharedPreferences: SharedPreferences): Typography {
    val fontFamily = getFontFamily(sharedPreferences.getString(SharedPreferencesUtils.FONT_FAMILY_KEY, ml.docilealligator.infinityforreddit.font.FontFamily.Default.name))
    val titleFontFamily = getFontFamily(sharedPreferences.getString(SharedPreferencesUtils.TITLE_FONT_FAMILY_KEY, ml.docilealligator.infinityforreddit.font.TitleFontFamily.Default.name))
    val contentFontFamily = getFontFamily(sharedPreferences.getString(SharedPreferencesUtils.CONTENT_FONT_FAMILY_KEY, ml.docilealligator.infinityforreddit.font.ContentFontFamily.Default.name))

    val fontSize = getFontSize(context, sharedPreferences)
    val titleFontSize = getTitleFontSize(context, sharedPreferences)
    val contentFontSize = getContentFontSize(context, sharedPreferences)

    return Typography(
        fontFamily, titleFontFamily, contentFontFamily, fontSize, titleFontSize, contentFontSize
    )
}

private fun getFontFamily(fontFamily: String?): FontFamily? {
    return when (fontFamily) {
        "Default" -> null
        "BalsamiqSans" -> FontFamily(Font(R.font.balsamiq_sans))
        "BalsamiqSansBold" -> FontFamily(Font(R.font.balsamiq_sans_bold_version))
        "NotoSans" -> FontFamily(Font(R.font.noto_sans))
        "NotoSansBold" -> FontFamily(Font(R.font.noto_sans_bold_version))
        "RobotoCondensed" -> FontFamily(Font(R.font.roboto_condensed))
        "RobotoCondensedBold" -> FontFamily(Font(R.font.roboto_condensed_bold_version))
        "HarmoniaSans" -> FontFamily(Font(R.font.harmonia_sans))
        "HarmoniaSansBold" -> FontFamily(Font(R.font.harmonia_sans_bold))
        "Inter" -> FontFamily(Font(R.font.inter))
        "InterBold" -> FontFamily(Font(R.font.inter_bold))
        "Manrope" -> FontFamily(Font(R.font.manrope))
        "ManropeBold" -> FontFamily(Font(R.font.manrope_bold))
        "Sriracha" -> FontFamily(Font(R.font.sriracha_regular))
        "AtkinsonHyperlegible" -> FontFamily(Font(R.font.atkinson_hyperlegible))
        "AtkinsonHyperlegibleBold" -> FontFamily(Font(R.font.atkinson_hyperlegible_bold_version))
        "Custom" -> FontFamily((context.applicationContext as Infinity).typeface)
        else -> null
    }
}

private fun getFontSize(context: Context, sharedPreferences: SharedPreferences): FontSize {
    return when (sharedPreferences.getString(SharedPreferencesUtils.FONT_SIZE_KEY, FontStyle.Normal.name)) {
        "XSmall" -> FontSize(10.sp, 8.sp, 10.sp, 12.sp, 14.sp, 16.sp)
        "Small" -> FontSize(12.sp, 10.sp, 12.sp, 14.sp, 16.sp, 18.sp)
        "Large" -> FontSize(16.sp, 12.sp, 14.sp, 18.sp, 20.sp, 22.sp)
        "XLarge" -> FontSize(18.sp, 14.sp, 16.sp, 20.sp, 22.sp, 24.sp)
        else -> FontSize(14.sp, 10.sp, 12.sp, 16.sp, 18.sp, 20.sp)
    }
}

private fun getTitleFontSize(context: Context, sharedPreferences: SharedPreferences): TitleFontSize {
    return when (sharedPreferences.getString(SharedPreferencesUtils.TITLE_FONT_SIZE_KEY, FontStyle.Normal.name)) {
        "XSmall" -> TitleFontSize(10.sp, 10.sp, 12.sp, 14.sp, 16.sp)
        "Small" -> TitleFontSize(12.sp, 12.sp, 14.sp, 16.sp, 18.sp)
        "Large" -> TitleFontSize(16.sp, 14.sp, 18.sp, 20.sp, 22.sp)
        "XLarge" -> TitleFontSize(18.sp, 16.sp, 20.sp, 22.sp, 24.sp)
        else -> TitleFontSize(14.sp, 12.sp, 16.sp, 18.sp, 20.sp)
    }
}

private fun getContentFontSize(context: Context, sharedPreferences: SharedPreferences): ContentFontSize {
    return when (sharedPreferences.getString(SharedPreferencesUtils.CONTENT_FONT_SIZE_KEY, FontStyle.Normal.name)) {
        "XSmall" -> ContentFontSize(10.sp, 10.sp, 12.sp, 14.sp, 16.sp)
        "Small" -> ContentFontSize(12.sp, 12.sp, 14.sp, 16.sp, 18.sp)
        "Large" -> ContentFontSize(16.sp, 14.sp, 18.sp, 20.sp, 22.sp)
        "XLarge" -> ContentFontSize(18.sp, 16.sp, 20.sp, 22.sp, 24.sp)
        "XXLarge" -> ContentFontSize(20.sp, 18.sp, 22.sp, 24.sp, 26.sp)
        else -> ContentFontSize(14.sp, 12.sp, 16.sp, 18.sp, 20.sp)
    }
}