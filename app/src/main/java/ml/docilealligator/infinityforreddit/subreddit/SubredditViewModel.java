package ml.docilealligator.infinityforreddit.subreddit;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubredditViewModel extends AndroidViewModel {
    private SubredditRepository mSubredditRepository;
    private LiveData<SubredditData> mSubredditLiveData;

    public SubredditViewModel(Application application, RedditDataRoomDatabase redditDataRoomDatabase, String id) {
        super(application);
        mSubredditRepository = new SubredditRepository(redditDataRoomDatabase, id);
        mSubredditLiveData = mSubredditRepository.getSubredditLiveData();
    }

    public LiveData<SubredditData> getSubredditLiveData() {
        return mSubredditLiveData;
    }

    public void insert(SubredditData subredditData) {
        mSubredditRepository.insert(subredditData);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @NonNull
        private final Application mApplication;
        private final RedditDataRoomDatabase mRedditDataRoomDatabase;
        private final String mSubredditName;

        public Factory(@NonNull Application application, RedditDataRoomDatabase redditDataRoomDatabase, String subredditname) {
            mApplication = application;
            mRedditDataRoomDatabase = redditDataRoomDatabase;
            mSubredditName = subredditname;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new SubredditViewModel(mApplication, mRedditDataRoomDatabase, mSubredditName);
        }
    }
}
