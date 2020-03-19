package ml.docilealligator.infinityforreddit.CustomTheme;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CustomThemeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CustomTheme customTheme);

    @Query("SELECT * FROM custom_themes")
    LiveData<List<CustomTheme>> getAllCustomThemes();

    @Query("SELECT * FROM custom_themes WHERE is_light_theme = 1 LIMIT 1")
    LiveData<CustomTheme> getLightCustomTheme();

    @Query("SELECT * FROM custom_themes WHERE is_dark_theme = 1 LIMIT 1")
    LiveData<CustomTheme> getDarkCustomTheme();

    @Query("SELECT * FROM custom_themes WHERE is_amoled_theme = 1 LIMIT 1")
    LiveData<CustomTheme> getAmoledCustomTheme();

    @Query("SELECT * FROM custom_themes WHERE name = :name AND username = :username COLLATE NOCASE LIMIT 1")
    CustomTheme getCustomTheme(String name, String username);

    @Query("DELETE FROM custom_themes WHERE name = :name AND username = :username")
    void deleteCustomTheme(String name, String username);
}
