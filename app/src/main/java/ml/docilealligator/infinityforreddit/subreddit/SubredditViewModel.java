package ml.docilealligator.infinityforreddit.subreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubredditViewModel extends ViewModel {
    private final SubredditRepository mSubredditRepository;
    private final LiveData<SubredditData> mSubredditLiveData;

    public SubredditViewModel(RedditDataRoomDatabase redditDataRoomDatabase, String id) {
        mSubredditRepository = new SubredditRepository(redditDataRoomDatabase, id);
        mSubredditLiveData = mSubredditRepository.getSubredditLiveData();
    }

    public LiveData<SubredditData> getSubredditLiveData() {
        return mSubredditLiveData;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final RedditDataRoomDatabase mRedditDataRoomDatabase;
        private final String mSubredditName;

        public Factory(RedditDataRoomDatabase redditDataRoomDatabase, String subredditname) {
            mRedditDataRoomDatabase = redditDataRoomDatabase;
            mSubredditName = subredditname;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection
            return (T) new SubredditViewModel(mRedditDataRoomDatabase, mSubredditName);
        }
    }
}
