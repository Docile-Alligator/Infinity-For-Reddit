package ml.docilealligator.infinityforreddit.recentsearchquery;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class RecentSearchQueryViewModel extends ViewModel {
    private LiveData<List<RecentSearchQuery>> mAllRecentSearchQueries;

    public RecentSearchQueryViewModel(RedditDataRoomDatabase redditDataRoomDatabase, String username) {
        mAllRecentSearchQueries = new RecentSearchQueryRepository(redditDataRoomDatabase, username).getAllRecentSearchQueries();
    }

    public LiveData<List<RecentSearchQuery>> getAllRecentSearchQueries() {
        return mAllRecentSearchQueries;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private RedditDataRoomDatabase mRedditDataRoomDatabase;
        private String mUsername;

        public Factory(RedditDataRoomDatabase redditDataRoomDatabase, String username) {
            mRedditDataRoomDatabase = redditDataRoomDatabase;
            mUsername = username;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new RecentSearchQueryViewModel(mRedditDataRoomDatabase, mUsername);
        }
    }
}
