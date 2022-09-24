package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
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
    private Executor executor;
    private Retrofit retrofit;
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private String accessToken;
    private String accountName;
    private SharedPreferences sharedPreferences;
    private int postType;
    private PostFilter postFilter;

    private LiveData<PagingData<Post>> posts;

    private MutableLiveData<PostFilter> postFilterLiveData;

    public HistoryPostViewModel(Executor executor, Retrofit retrofit, RedditDataRoomDatabase redditDataRoomDatabase,
                                String accessToken, String accountName, SharedPreferences sharedPreferences,
                                int postType, PostFilter postFilter) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postType = postType;
        this.postFilter = postFilter;

        postFilterLiveData = new MutableLiveData<>();
        postFilterLiveData.postValue(postFilter);

        Pager<String, Post> pager = new Pager<>(new PagingConfig(25, 25, false), this::returnPagingSoruce);

        posts = Transformations.switchMap(postFilterLiveData, postFilterValue -> PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this)));
    }

    public LiveData<PagingData<Post>> getPosts() {
        return posts;
    }

    public HistoryPostPagingSource returnPagingSoruce() {
        HistoryPostPagingSource paging3PagingSource;
        switch (postType) {
            case HistoryPostPagingSource.TYPE_READ_POSTS:
                paging3PagingSource = new HistoryPostPagingSource(retrofit, executor, redditDataRoomDatabase, accessToken, accountName,
                        sharedPreferences, accountName, postType, postFilter);
                break;
            default:
                paging3PagingSource = new HistoryPostPagingSource(retrofit, executor, redditDataRoomDatabase, accessToken, accountName,
                        sharedPreferences, accountName, postType, postFilter);
                break;
        }
        return paging3PagingSource;
    }

    public void changePostFilter(PostFilter postFilter) {
        postFilterLiveData.postValue(postFilter);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Executor executor;
        private Retrofit retrofit;
        private RedditDataRoomDatabase redditDataRoomDatabase;
        private String accessToken;
        private String accountName;
        private SharedPreferences sharedPreferences;
        private int postType;
        private PostFilter postFilter;

        public Factory(Executor executor, Retrofit retrofit, RedditDataRoomDatabase redditDataRoomDatabase,
                       String accessToken, String accountName, SharedPreferences sharedPreferences, int postType,
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
