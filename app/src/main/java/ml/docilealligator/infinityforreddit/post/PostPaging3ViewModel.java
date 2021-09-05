package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import retrofit2.Retrofit;

public class PostPaging3ViewModel extends ViewModel {

    private PostPaging3Repository repository;
    private LiveData<PagingData<Post>> posts;
    private PostPaging3PagingSource paging3PagingSource;
    private Lifecycle lifecycle;

    public PostPaging3ViewModel(Lifecycle lifecycle, Executor executor, Retrofit retrofit, String accessToken, String accountName,
                                SharedPreferences sharedPreferences,
                                SharedPreferences postFeedScrolledPositionSharedPreferences,
                                String subredditOrUserName, String query, String trendingSource, int postType,
                                SortType sortType, PostFilter postFilter, List<ReadPost> readPostList,
                                String userWhere, String multiRedditPath, LinkedHashSet<Post> postLinkedHashSet) {
        repository = new PostPaging3Repository(executor, retrofit, accessToken, accountName, sharedPreferences, postFeedScrolledPositionSharedPreferences,
                subredditOrUserName, query, trendingSource, postType, sortType, postFilter, readPostList, userWhere, multiRedditPath, postLinkedHashSet);
        paging3PagingSource = repository.returnPagingSoruce();
        Pager<String, Post> pager = new Pager<>(new PagingConfig(25, 25, false), this::returnPagingSoruce);
        posts = PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this));
    }

    public LiveData<PagingData<Post>> getPosts() {
        /*Pager<String, Post> pager = new Pager<>(new PagingConfig(25, 25, false), this::returnPagingSoruce);
        posts = PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this));
        posts = PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), lifecycle);*/
        return posts;
    }

    private PostPaging3PagingSource returnPagingSoruce() {
        return paging3PagingSource;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Lifecycle lifecycle;
        private Executor executor;
        private Retrofit retrofit;
        private String accessToken;
        private String accountName;
        private SharedPreferences sharedPreferences;
        private SharedPreferences postFeedScrolledPositionSharedPreferences;
        private String subredditOrUserName;
        private String query;
        private String trendingSource;
        private int postType;
        private SortType sortType;
        private PostFilter postFilter;
        private List<ReadPost> readPostList;
        private String userWhere;
        private String multiRedditPath;
        private LinkedHashSet<Post> postLinkedHashSet;

        public Factory(Lifecycle lifecycle, Executor executor, Retrofit retrofit, String accessToken, String accountName, SharedPreferences sharedPreferences,
                       SharedPreferences postFeedScrolledPositionSharedPreferences, String subredditOrUserName,
                       String query, String trendingSource, int postType, SortType sortType, PostFilter postFilter,
                       List<ReadPost> readPostList, String userWhere, String multiRedditPath,
                       LinkedHashSet<Post> postLinkedHashSet) {
            this.lifecycle = lifecycle;
            this.executor = executor;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.subredditOrUserName = subredditOrUserName;
            this.query = query;
            this.trendingSource = trendingSource;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            this.readPostList = readPostList;
            this.userWhere = userWhere;
            this.multiRedditPath = multiRedditPath;
            this.postLinkedHashSet = postLinkedHashSet;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new PostPaging3ViewModel(lifecycle, executor, retrofit, accessToken, accountName, sharedPreferences,
                    postFeedScrolledPositionSharedPreferences, subredditOrUserName, query, trendingSource,
                    postType, sortType, postFilter, readPostList, userWhere, multiRedditPath, postLinkedHashSet);
        }
    }
}
