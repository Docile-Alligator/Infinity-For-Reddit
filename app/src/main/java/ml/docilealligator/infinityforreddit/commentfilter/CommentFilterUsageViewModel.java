package ml.docilealligator.infinityforreddit.commentfilter;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

public class CommentFilterUsageViewModel extends ViewModel {
    private LiveData<List<CommentFilterUsage>> mCommentFilterUsageListLiveData;

    public CommentFilterUsageViewModel(RedditDataRoomDatabase redditDataRoomDatabase, String name) {
        mCommentFilterUsageListLiveData = redditDataRoomDatabase.commentFilterUsageDao().getAllCommentFilterUsageLiveData(name);
    }

    public LiveData<List<CommentFilterUsage>> getCommentFilterUsageListLiveData() {
        return mCommentFilterUsageListLiveData;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private final RedditDataRoomDatabase mRedditDataRoomDatabase;
        private final String mName;

        public Factory(RedditDataRoomDatabase redditDataRoomDatabase, String name) {
            mRedditDataRoomDatabase = redditDataRoomDatabase;
            mName = name;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            //noinspection unchecked
            return (T) new CommentFilterUsageViewModel(mRedditDataRoomDatabase, mName);
        }
    }
}
