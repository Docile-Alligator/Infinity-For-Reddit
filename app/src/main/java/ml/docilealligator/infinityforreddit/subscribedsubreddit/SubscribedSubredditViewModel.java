package ml.docilealligator.infinityforreddit.subscribedsubreddit;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubscribedSubredditViewModel extends AndroidViewModel {
    private SubscribedSubredditRepository mSubscribedSubredditRepository;
    private LiveData<List<SubscribedSubredditData>> mAllSubscribedSubreddits;
    private LiveData<List<SubscribedSubredditData>> mAllFavoriteSubscribedSubreddits;
    private MutableLiveData<String> searchQueryLiveData;

    public SubscribedSubredditViewModel(Application application, RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
        super(application);
        mSubscribedSubredditRepository = new SubscribedSubredditRepository(redditDataRoomDatabase, accountName);
        searchQueryLiveData = new MutableLiveData<>();
        searchQueryLiveData.postValue("");

        mAllSubscribedSubreddits = Transformations.switchMap(searchQueryLiveData, searchQuery -> mSubscribedSubredditRepository.getAllSubscribedSubredditsWithSearchQuery(searchQuery));
        mAllFavoriteSubscribedSubreddits = Transformations.switchMap(searchQueryLiveData, searchQuery -> mSubscribedSubredditRepository.getAllFavoriteSubscribedSubredditsWithSearchQuery(searchQuery));
    }

    public LiveData<List<SubscribedSubredditData>> getAllSubscribedSubreddits() {
        return mAllSubscribedSubreddits;
    }

    public LiveData<List<SubscribedSubredditData>> getAllFavoriteSubscribedSubreddits() {
        return mAllFavoriteSubscribedSubreddits;
    }

    public void insert(SubscribedSubredditData subscribedSubredditData) {
        mSubscribedSubredditRepository.insert(subscribedSubredditData);
    }

    public void setSearchQuery(String searchQuery) {
        searchQueryLiveData.postValue(searchQuery);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Application mApplication;
        private RedditDataRoomDatabase mRedditDataRoomDatabase;
        private String mAccountName;

        public Factory(Application application, RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
            this.mApplication = application;
            this.mRedditDataRoomDatabase = redditDataRoomDatabase;
            this.mAccountName = accountName;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SubscribedSubredditViewModel(mApplication, mRedditDataRoomDatabase, mAccountName);
        }
    }
}
