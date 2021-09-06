package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import retrofit2.Retrofit;

public class PostPaging3Repository {
    Executor executor;
    Retrofit retrofit;
    String accessToken;
    String accountName;
    SharedPreferences sharedPreferences;
    SharedPreferences postFeedScrolledPositionSharedPreferences;
    int postType;
    SortType sortType;
    PostFilter postFilter;
    List<ReadPost> readPostList;
    String name;
    String userWhere;
    String query;
    String trendingSource;

    private PostPaging3PagingSource paging3PagingSource;
    private int type;

    PostPaging3Repository(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                          SharedPreferences sharedPreferences,
                          SharedPreferences postFeedScrolledPositionSharedPreferences, int postType,
                          SortType sortType, PostFilter postFilter, List<ReadPost> readPostList) {
        type = 1;
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.postType = postType;
        this.sortType = sortType;
        this.postFilter = postFilter;
        this.readPostList = readPostList;
    }

    PostPaging3Repository(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                          SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                          String name, int postType, SortType sortType, PostFilter postFilter,
                          List<ReadPost> readPostList) {
        type = 2;
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.postType = postType;
        this.sortType = sortType;
        this.postFilter = postFilter;
        this.readPostList = readPostList;
        this.name = name;
    }

    PostPaging3Repository(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                          SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                          String name, int postType, SortType sortType, PostFilter postFilter,
                          String userWhere, List<ReadPost> readPostList) {
        type = 3;
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.postType = postType;
        this.sortType = sortType;
        this.postFilter = postFilter;
        this.readPostList = readPostList;
        this.name = name;
        this.userWhere = userWhere;
    }

    PostPaging3Repository(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                          SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                          String name, String query, String trendingSource, int postType, SortType sortType,
                          PostFilter postFilter, List<ReadPost> readPostList) {
        type = 4;
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.postType = postType;
        this.sortType = sortType;
        this.postFilter = postFilter;
        this.readPostList = readPostList;
        this.name = name;
        this.query = query;
        this.trendingSource = trendingSource;
    }

    public PostPaging3PagingSource returnPagingSoruce() {
        switch (type) {
            case 1:
                paging3PagingSource = new PostPaging3PagingSource(executor, retrofit, accessToken, accountName,
                        sharedPreferences, postFeedScrolledPositionSharedPreferences, postType, sortType,
                        postFilter, readPostList);
                break;
            case 2:
                paging3PagingSource = new PostPaging3PagingSource(executor, retrofit, accessToken, accountName,
                        sharedPreferences, postFeedScrolledPositionSharedPreferences, name, postType,
                        sortType, postFilter, readPostList);
                break;
            case 3:
                paging3PagingSource = new PostPaging3PagingSource(executor, retrofit, accessToken, accountName,
                        sharedPreferences, postFeedScrolledPositionSharedPreferences, name, postType,
                        sortType, postFilter, userWhere, readPostList);
                break;
            default:
                paging3PagingSource = new PostPaging3PagingSource(executor, retrofit, accessToken, accountName,
                        sharedPreferences, postFeedScrolledPositionSharedPreferences, name, query, trendingSource,
                        postType, sortType, postFilter, readPostList);
        }
        return paging3PagingSource;
    }

    void changeSortTypeAndPostFilter(SortType sortType, PostFilter postFilter) {
        this.sortType = sortType;
        this.postFilter = postFilter;
    }
}
