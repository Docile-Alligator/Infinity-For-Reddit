package ml.docilealligator.infinityforreddit.customtheme

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomThemeDaoKt {
    @Query("SELECT * FROM custom_themes WHERE is_light_theme = 1 LIMIT 1")
    fun getLightCustomThemeFlow(): Flow<CustomTheme?>

    @Query("SELECT * FROM custom_themes WHERE is_dark_theme = 1 LIMIT 1")
    fun getDarkCustomThemeFlow(): Flow<CustomTheme?>

    @Query("SELECT * FROM custom_themes WHERE is_amoled_theme = 1 LIMIT 1")
    fun getAmoledCustomThemeFlow(): Flow<CustomTheme?>
}