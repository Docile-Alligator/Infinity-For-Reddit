package ml.docilealligator.infinityforreddit.subscribedsubreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubscribedSubredditViewModel extends ViewModel {
    private final SubscribedSubredditRepository mSubscribedSubredditRepository;
    private final LiveData<List<SubscribedSubredditData>> mAllSubscribedSubreddits;
    private final LiveData<List<SubscribedSubredditData>> mAllFavoriteSubscribedSubreddits;
    private final MutableLiveData<String> searchQueryLiveData;

    public SubscribedSubredditViewModel(RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
        mSubscribedSubredditRepository = new SubscribedSubredditRepository(redditDataRoomDatabase, accountName);
        searchQueryLiveData = new MutableLiveData<>("");

        mAllSubscribedSubreddits = Transformations.switchMap(searchQueryLiveData, mSubscribedSubredditRepository::getAllSubscribedSubredditsWithSearchQuery);
        mAllFavoriteSubscribedSubreddits = Transformations.switchMap(searchQueryLiveData, mSubscribedSubredditRepository::getAllFavoriteSubscribedSubredditsWithSearchQuery);
    }

    public LiveData<List<SubscribedSubredditData>> getAllSubscribedSubreddits() {
        return mAllSubscribedSubreddits;
    }

    public LiveData<List<SubscribedSubredditData>> getAllFavoriteSubscribedSubreddits() {
        return mAllFavoriteSubscribedSubreddits;
    }

    public void setSearchQuery(String searchQuery) {
        searchQueryLiveData.postValue(searchQuery);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final RedditDataRoomDatabase mRedditDataRoomDatabase;
        private final String mAccountName;

        public Factory(RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
            this.mRedditDataRoomDatabase = redditDataRoomDatabase;
            this.mAccountName = accountName;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SubscribedSubredditViewModel(mRedditDataRoomDatabase, mAccountName);
        }
    }
}
