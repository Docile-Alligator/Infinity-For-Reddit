package ml.docilealligator.infinityforreddit.customtheme;

import androidx.lifecycle.LiveData;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class LocalCustomThemeRepository {
    private final LiveData<List<CustomTheme>> mAllCustomThemes;
    private final LiveData<CustomTheme> mCurrentLightCustomTheme;
    private final LiveData<CustomTheme> mCurrentDarkCustomTheme;
    private final LiveData<CustomTheme> mCurrentAmoledCustomTheme;

    LocalCustomThemeRepository(RedditDataRoomDatabase redditDataRoomDatabase) {
        mAllCustomThemes = redditDataRoomDatabase.customThemeDao().getAllCustomThemes();
        mCurrentLightCustomTheme = redditDataRoomDatabase.customThemeDao().getLightCustomThemeLiveData();
        mCurrentDarkCustomTheme = redditDataRoomDatabase.customThemeDao().getDarkCustomThemeLiveData();
        mCurrentAmoledCustomTheme = redditDataRoomDatabase.customThemeDao().getAmoledCustomThemeLiveData();
    }

    LiveData<List<CustomTheme>> getAllCustomThemes() {
        return mAllCustomThemes;
    }

    LiveData<CustomTheme> getCurrentLightCustomTheme() {
        return mCurrentLightCustomTheme;
    }

    LiveData<CustomTheme> getCurrentDarkCustomTheme() {
        return mCurrentDarkCustomTheme;
    }

    LiveData<CustomTheme> getCurrentAmoledCustomTheme() {
        return mCurrentAmoledCustomTheme;
    }
}
