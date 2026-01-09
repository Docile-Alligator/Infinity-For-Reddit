package ml.docilealligator.infinityforreddit.customviews.compose

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.map
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customtheme.LocalCustomThemeRepository
import ml.docilealligator.infinityforreddit.utils.CustomThemeSharedPreferencesUtils

val LocalAppTheme = staticCompositionLocalOf<CustomTheme> {
    error("No default theme values")
}

@Composable
fun AppTheme(themeType: Int, content: @Composable () -> Unit) {
    val context = LocalContext.current
    val localCustomThemeRepository = LocalCustomThemeRepository(((context.applicationContext) as Infinity).mRedditDataRoomDatabase)

    val currentThemeFlow = when(themeType) {
        CustomThemeSharedPreferencesUtils.LIGHT -> localCustomThemeRepository.currentLightCustomThemeFlow
        CustomThemeSharedPreferencesUtils.DARK -> localCustomThemeRepository.currentDarkCustomThemeFlow
        CustomThemeSharedPreferencesUtils.AMOLED -> localCustomThemeRepository.currentAmoledCustomThemeFlow
        else -> localCustomThemeRepository.currentLightCustomThemeFlow
    }

    val customTheme by currentThemeFlow.collectAsState(initial = null)
    customTheme?.let {
        CompositionLocalProvider(LocalAppTheme provides it) {
            MaterialTheme {
                content()
            }
        }
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