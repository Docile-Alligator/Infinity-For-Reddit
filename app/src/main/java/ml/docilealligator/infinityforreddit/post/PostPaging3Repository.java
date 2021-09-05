package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
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

public class PostPaging3Repository {
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
    private PostPaging3PagingSource paging3PagingSource;

    public PostPaging3Repository(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                                 SharedPreferences sharedPreferences,
                                 SharedPreferences postFeedScrolledPositionSharedPreferences,
                                 String subredditOrUserName, String query, String trendingSource, int postType,
                                 SortType sortType, PostFilter postFilter, List<ReadPost> readPostList,
                                 String userWhere, String multiRedditPath, LinkedHashSet<Post> postLinkedHashSet) {
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
        paging3PagingSource = new PostPaging3PagingSource(executor, retrofit, accessToken, accountName, sharedPreferences,
                postFeedScrolledPositionSharedPreferences, postType, sortType, postFilter, readPostList);
    }

    public LiveData<PagingData<Post>> getPostsLiveData() {
        Pager<String, Post> pager = new Pager<>(new PagingConfig(25, 25, false), this::returnPagingSoruce);
        return PagingLiveData.getLiveData(pager);
    }

    public PostPaging3PagingSource returnPagingSoruce() {
        return paging3PagingSource;
    }
}
