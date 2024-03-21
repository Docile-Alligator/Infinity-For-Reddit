package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import retrofit2.Retrofit;

public class HistoryPostViewModel extends ViewModel {
    private final Executor executor;
    private final Retrofit retrofit;
    private final RedditDataRoomDatabase redditDataRoomDatabase;
    private final String accessToken;
    private final String accountName;
    private final SharedPreferences sharedPreferences;
    private final int postType;
    private final PostFilter postFilter;

    private final LiveData<PagingData<Post>> posts;

    private final MutableLiveData<PostFilter> postFilterLiveData;

    public HistoryPostViewModel(Executor executor, Retrofit retrofit, RedditDataRoomDatabase redditDataRoomDatabase,
                                @Nullable String accessToken, @NonNull String accountName, SharedPreferences sharedPreferences,
                                int postType, PostFilter postFilter) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postType = postType;
        this.postFilter = postFilter;

        postFilterLiveData = new MutableLiveData<>(postFilter);

        Pager<String, Post> pager = new Pager<>(new PagingConfig(25, 4, false, 10), this::returnPagingSource);

        posts = Transformations.switchMap(postFilterLiveData, postFilterValue -> PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this)));
    }

    public LiveData<PagingData<Post>> getPosts() {
        return posts;
    }

    public HistoryPostPagingSource returnPagingSource() {
        HistoryPostPagingSource historyPostPagingSource;
        switch (postType) {
            case HistoryPostPagingSource.TYPE_READ_POSTS:
                historyPostPagingSource = new HistoryPostPagingSource(retrofit, executor, redditDataRoomDatabase, accessToken, accountName,
                        sharedPreferences, accountName, postType, postFilter);
                break;
            default:
                historyPostPagingSource = new HistoryPostPagingSource(retrofit, executor, redditDataRoomDatabase, accessToken, accountName,
                        sharedPreferences, accountName, postType, postFilter);
                break;
        }
        return historyPostPagingSource;
    }

    public void changePostFilter(PostFilter postFilter) {
        postFilterLiveData.postValue(postFilter);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final Executor executor;
        private final Retrofit retrofit;
        private final RedditDataRoomDatabase redditDataRoomDatabase;
        private final String accessToken;
        private final String accountName;
        private final SharedPreferences sharedPreferences;
        private final int postType;
        private final PostFilter postFilter;

        public Factory(Executor executor, Retrofit retrofit, RedditDataRoomDatabase redditDataRoomDatabase,
                       @Nullable String accessToken, @NonNull String accountName, SharedPreferences sharedPreferences, int postType,
                       PostFilter postFilter) {
            this.executor = executor;
            this.retrofit = retrofit;
            this.redditDataRoomDatabase = redditDataRoomDatabase;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postType = postType;
            this.postFilter = postFilter;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (postType == HistoryPostPagingSource.TYPE_READ_POSTS) {
                return (T) new HistoryPostViewModel(executor, retrofit, redditDataRoomDatabase, accessToken, accountName, sharedPreferences,
                        postType, postFilter);
            } else {
                return (T) new HistoryPostViewModel(executor, retrofit, redditDataRoomDatabase, accessToken, accountName, sharedPreferences,
                        postType, postFilter);
            }
        }
    }
}
