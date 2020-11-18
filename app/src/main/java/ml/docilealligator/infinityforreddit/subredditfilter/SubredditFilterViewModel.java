package ml.docilealligator.infinityforreddit.subredditfilter;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class SubredditFilterViewModel extends ViewModel {
    private LiveData<List<SubredditFilter>> subredditFilterLiveData;

    public SubredditFilterViewModel(RedditDataRoomDatabase redditDataRoomDatabase) {
        subredditFilterLiveData = redditDataRoomDatabase.subredditFilterDao().getAllSubredditFiltersLiveData();
    }

    public LiveData<List<SubredditFilter>> getSubredditFilterLiveData() {
        return subredditFilterLiveData;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private RedditDataRoomDatabase redditDataRoomDatabase;

        public Factory(RedditDataRoomDatabase redditDataRoomDatabase) {
            this.redditDataRoomDatabase = redditDataRoomDatabase;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SubredditFilterViewModel(redditDataRoomDatabase);
        }
    }
}
