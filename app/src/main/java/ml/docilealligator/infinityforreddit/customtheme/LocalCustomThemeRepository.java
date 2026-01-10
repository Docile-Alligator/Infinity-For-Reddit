package ml.docilealligator.infinityforreddit.customtheme;

import androidx.lifecycle.LiveData;

import java.util.List;

import kotlinx.coroutines.flow.Flow;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class LocalCustomThemeRepository {
    private final LiveData<List<CustomTheme>> mAllCustomThemes;
    private final LiveData<CustomTheme> mCurrentLightCustomTheme;
    private final LiveData<CustomTheme> mCurrentDarkCustomTheme;
    private final LiveData<CustomTheme> mCurrentAmoledCustomTheme;

    private final Flow<CustomTheme> mCurrentLightCustomThemeFlow;
    private final Flow<CustomTheme> mCurrentDarkCustomThemeFlow;
    private final Flow<CustomTheme> mCurrentAmoledCustomThemeFlow;

    public LocalCustomThemeRepository(RedditDataRoomDatabase redditDataRoomDatabase) {
        mAllCustomThemes = redditDataRoomDatabase.customThemeDao().getAllCustomThemes();
        mCurrentLightCustomTheme = redditDataRoomDatabase.customThemeDao().getLightCustomThemeLiveData();
        mCurrentDarkCustomTheme = redditDataRoomDatabase.customThemeDao().getDarkCustomThemeLiveData();
        mCurrentAmoledCustomTheme = redditDataRoomDatabase.customThemeDao().getAmoledCustomThemeLiveData();

        mCurrentLightCustomThemeFlow = redditDataRoomDatabase.customThemeDaoKt().getLightCustomThemeFlow();
        mCurrentDarkCustomThemeFlow = redditDataRoomDatabase.customThemeDaoKt().getDarkCustomThemeFlow();
        mCurrentAmoledCustomThemeFlow = redditDataRoomDatabase.customThemeDaoKt().getAmoledCustomThemeFlow();
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

    public Flow<CustomTheme> getCurrentLightCustomThemeFlow() {
        return mCurrentLightCustomThemeFlow;
    }

    public Flow<CustomTheme> getCurrentDarkCustomThemeFlow() {
        return mCurrentDarkCustomThemeFlow;
    }

    public Flow<CustomTheme> getCurrentAmoledCustomThemeFlow() {
        return mCurrentAmoledCustomThemeFlow;
    }
}
