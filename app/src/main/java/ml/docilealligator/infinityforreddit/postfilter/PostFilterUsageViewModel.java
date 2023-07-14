package ml.docilealligator.infinityforreddit.postfilter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class PostFilterUsageViewModel extends ViewModel {
    private LiveData<List<PostFilterUsage>> mPostFilterUsageListLiveData;

    public PostFilterUsageViewModel(RedditDataRoomDatabase redditDataRoomDatabase, String name) {
        mPostFilterUsageListLiveData = redditDataRoomDatabase.postFilterUsageDao().getAllPostFilterUsageLiveData(name);
    }

    public LiveData<List<PostFilterUsage>> getPostFilterUsageListLiveData() {
        return mPostFilterUsageListLiveData;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final RedditDataRoomDatabase mRedditDataRoomDatabase;
        private final String mName;

        public Factory(RedditDataRoomDatabase redditDataRoomDatabase, String name) {
            mRedditDataRoomDatabase = redditDataRoomDatabase;
            mName = name;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new PostFilterUsageViewModel(mRedditDataRoomDatabase, mName);
        }
    }
}
