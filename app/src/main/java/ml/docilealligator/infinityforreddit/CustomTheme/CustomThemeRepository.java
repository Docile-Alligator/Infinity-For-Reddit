package ml.docilealligator.infinityforreddit.CustomTheme;

import androidx.lifecycle.LiveData;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class CustomThemeRepository {
    private LiveData<List<CustomTheme>> mAllCustomThemes;
    private LiveData<CustomTheme> mLightCustomTheme;
    private LiveData<CustomTheme> mDarkCustomTheme;
    private LiveData<CustomTheme> mAmoledCustomTheme;

    CustomThemeRepository(RedditDataRoomDatabase redditDataRoomDatabase) {
        mAllCustomThemes = redditDataRoomDatabase.customThemeDao().getAllCustomThemes();
        mLightCustomTheme = redditDataRoomDatabase.customThemeDao().getLightCustomTheme();
        mDarkCustomTheme = redditDataRoomDatabase.customThemeDao().getDarkCustomTheme();
        mAmoledCustomTheme = redditDataRoomDatabase.customThemeDao().getAmoledCustomTheme();
    }

    LiveData<List<CustomTheme>> getAllCustomThemes() {
        return mAllCustomThemes;
    }

    public LiveData<CustomTheme> getLightCustomTheme() {
        return mLightCustomTheme;
    }

    public LiveData<CustomTheme> getDarkCustomTheme() {
        return mDarkCustomTheme;
    }

    public LiveData<CustomTheme> getAmoledCustomTheme() {
        return mAmoledCustomTheme;
    }
}
