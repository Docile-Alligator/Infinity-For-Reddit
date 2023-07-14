package ml.docilealligator.infinityforreddit.postfilter;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class PostFilterViewModel extends ViewModel {
    private LiveData<List<PostFilter>> mPostFilterListLiveData;

    public PostFilterViewModel(RedditDataRoomDatabase redditDataRoomDatabase) {
        mPostFilterListLiveData = redditDataRoomDatabase.postFilterDao().getAllPostFiltersLiveData();
    }

    public LiveData<List<PostFilter>> getPostFilterListLiveData() {
        return mPostFilterListLiveData;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final RedditDataRoomDatabase mRedditDataRoomDatabase;

        public Factory(RedditDataRoomDatabase redditDataRoomDatabase) {
            mRedditDataRoomDatabase = redditDataRoomDatabase;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            //noinspection unchecked
            return (T) new PostFilterViewModel(mRedditDataRoomDatabase);
        }
    }
}
