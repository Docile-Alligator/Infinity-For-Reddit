package ml.docilealligator.infinityforreddit.customtheme;

import androidx.lifecycle.LiveData;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class CustomThemeRepository {
    private LiveData<List<CustomTheme>> mAllCustomThemes;
    private LiveData<CustomTheme> mCurrentLightCustomTheme;
    private LiveData<CustomTheme> mCurrentDarkCustomTheme;
    private LiveData<CustomTheme> mCurrentAmoledCustomTheme;

    CustomThemeRepository(RedditDataRoomDatabase redditDataRoomDatabase) {
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
