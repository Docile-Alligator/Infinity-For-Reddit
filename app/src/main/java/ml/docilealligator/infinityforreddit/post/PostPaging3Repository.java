package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import retrofit2.Retrofit;

public class PostPaging3Repository {
    private PostPaging3PagingSource paging3PagingSource;

    PostPaging3Repository(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                          SharedPreferences sharedPreferences,
                          SharedPreferences postFeedScrolledPositionSharedPreferences, int postType,
                          SortType sortType, PostFilter postFilter, List<ReadPost> readPostList) {
        paging3PagingSource = new PostPaging3PagingSource(executor, retrofit, accessToken, accountName,
                sharedPreferences, postFeedScrolledPositionSharedPreferences, postType, sortType,
                postFilter, readPostList);
    }

    PostPaging3Repository(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                          SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                          String name, int postType, SortType sortType, PostFilter postFilter,
                          List<ReadPost> readPostList) {
        paging3PagingSource = new PostPaging3PagingSource(executor, retrofit, accessToken, accountName,
                sharedPreferences, postFeedScrolledPositionSharedPreferences, name, postType,
                sortType, postFilter, readPostList);
    }

    PostPaging3Repository(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                          SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                          String name, int postType, SortType sortType, PostFilter postFilter,
                          String userWhere, List<ReadPost> readPostList) {

        paging3PagingSource = new PostPaging3PagingSource(executor, retrofit, accessToken, accountName,
                sharedPreferences, postFeedScrolledPositionSharedPreferences, name, postType,
                sortType, postFilter, userWhere, readPostList);
    }

    PostPaging3Repository(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                          SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                          String name, String query, String trendingSource, int postType, SortType sortType,
                          PostFilter postFilter, List<ReadPost> readPostList) {
        paging3PagingSource = new PostPaging3PagingSource(executor, retrofit, accessToken, accountName,
                sharedPreferences, postFeedScrolledPositionSharedPreferences, name, query, trendingSource,
                postType, sortType, postFilter, readPostList);
    }

    public PostPaging3PagingSource returnPagingSoruce() {
        return paging3PagingSource;
    }
}
