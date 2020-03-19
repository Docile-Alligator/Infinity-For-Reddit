package ml.docilealligator.infinityforreddit.CustomTheme;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class CustomThemeViewModel extends ViewModel {
    private LiveData<List<CustomTheme>> mAllCustomThemes;
    private LiveData<CustomTheme> mLightCustomTheme;
    private LiveData<CustomTheme> mDarkCustomTheme;
    private LiveData<CustomTheme> mAmoledCustomTheme;

    public CustomThemeViewModel(RedditDataRoomDatabase redditDataRoomDatabase) {
        CustomThemeRepository customThemeRepository = new CustomThemeRepository(redditDataRoomDatabase);
        mAllCustomThemes = customThemeRepository.getAllCustomThemes();
        mLightCustomTheme = customThemeRepository.getLightCustomTheme();
        mDarkCustomTheme = customThemeRepository.getDarkCustomTheme();
        mAmoledCustomTheme = customThemeRepository.getAmoledCustomTheme();
    }

    public LiveData<List<CustomTheme>> getAllCustomThemes() {
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

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private RedditDataRoomDatabase mRedditDataRoomDatabase;

        public Factory(RedditDataRoomDatabase redditDataRoomDatabase) {
            mRedditDataRoomDatabase = redditDataRoomDatabase;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CustomThemeViewModel(mRedditDataRoomDatabase);
        }
    }
}
