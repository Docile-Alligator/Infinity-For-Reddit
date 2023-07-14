package ml.docilealligator.infinityforreddit.multireddit;

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

public class MultiRedditViewModel extends AndroidViewModel {
    private MultiRedditRepository mMultiRedditRepository;
    private LiveData<List<MultiReddit>> mAllMultiReddits;
    private LiveData<List<MultiReddit>> mAllFavoriteMultiReddits;
    private MutableLiveData<String> searchQueryLiveData;

    public MultiRedditViewModel(Application application, RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
        super(application);
        mMultiRedditRepository = new MultiRedditRepository(redditDataRoomDatabase, accountName);
        searchQueryLiveData = new MutableLiveData<>();
        searchQueryLiveData.postValue("");

        mAllMultiReddits = Transformations.switchMap(searchQueryLiveData, searchQuery -> mMultiRedditRepository.getAllMultiRedditsWithSearchQuery(searchQuery));
        mAllFavoriteMultiReddits = Transformations.switchMap(searchQueryLiveData, searchQuery -> mMultiRedditRepository.getAllFavoriteMultiRedditsWithSearchQuery(searchQuery));
    }

    public LiveData<List<MultiReddit>> getAllMultiReddits() {
        return mAllMultiReddits;
    }

    public LiveData<List<MultiReddit>> getAllFavoriteMultiReddits() {
        return mAllFavoriteMultiReddits;
    }

    public void setSearchQuery(String searchQuery) {
        searchQueryLiveData.postValue(searchQuery);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Application mApplication;
        private RedditDataRoomDatabase mRedditDataRoomDatabase;
        private String mAccountName;

        public Factory(Application application, RedditDataRoomDatabase redditDataRoomDatabase, String accountName) {
            mApplication = application;
            mRedditDataRoomDatabase = redditDataRoomDatabase;
            mAccountName = accountName;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new MultiRedditViewModel(mApplication, mRedditDataRoomDatabase, mAccountName);
        }
    }
}
