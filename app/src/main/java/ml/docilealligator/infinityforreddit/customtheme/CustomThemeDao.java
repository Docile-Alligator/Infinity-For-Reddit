package ml.docilealligator.infinityforreddit.customtheme;

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CustomTheme> customThemes);

    @Query("SELECT * FROM custom_themes")
    LiveData<List<CustomTheme>> getAllCustomThemes();

    @Query("SELECT * FROM custom_themes")
    List<CustomTheme> getAllCustomThemesList();

    @Query("SELECT * FROM custom_themes WHERE is_light_theme = 1 LIMIT 1")
    CustomTheme getLightCustomTheme();

    @Query("SELECT * FROM custom_themes WHERE is_dark_theme = 1 LIMIT 1")
    CustomTheme getDarkCustomTheme();

    @Query("SELECT * FROM custom_themes WHERE is_amoled_theme = 1 LIMIT 1")
    CustomTheme getAmoledCustomTheme();

    @Query("SELECT * FROM custom_themes WHERE is_light_theme = 1 LIMIT 1")
    LiveData<CustomTheme> getLightCustomThemeLiveData();

    @Query("SELECT * FROM custom_themes WHERE is_dark_theme = 1 LIMIT 1")
    LiveData<CustomTheme> getDarkCustomThemeLiveData();

    @Query("SELECT * FROM custom_themes WHERE is_amoled_theme = 1 LIMIT 1")
    LiveData<CustomTheme> getAmoledCustomThemeLiveData();

    @Query("SELECT * FROM custom_themes WHERE name = :name COLLATE NOCASE LIMIT 1")
    CustomTheme getCustomTheme(String name);

    @Query("UPDATE custom_themes SET is_light_theme = 0 WHERE is_light_theme = 1")
    void unsetLightTheme();

    @Query("UPDATE custom_themes SET is_dark_theme = 0 WHERE is_dark_theme = 1")
    void unsetDarkTheme();

    @Query("UPDATE custom_themes SET is_amoled_theme = 0 WHERE is_amoled_theme = 1")
    void unsetAmoledTheme();

    @Query("DELETE FROM custom_themes WHERE name = :name")
    void deleteCustomTheme(String name);

    @Query("UPDATE custom_themes SET name = :newName WHERE name = :oldName")
    void updateName(String oldName, String newName);

    @Query("DELETE FROM custom_themes")
    void deleteAllCustomThemes();
}
