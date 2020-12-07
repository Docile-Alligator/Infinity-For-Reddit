package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import java.util.List;
import java.util.Locale;

import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import ml.docilealligator.infinityforreddit.subredditfilter.SubredditFilter;
import retrofit2.Retrofit;

class PostDataSourceFactory extends DataSource.Factory {
    private Retrofit retrofit;
    private String accessToken;
    private String accountName;
    private Locale locale;
    private SharedPreferences sharedPreferences;
    private SharedPreferences postFeedScrolledPositionSharedPreferences;
    private String subredditName;
    private String query;
    private int postType;
    private SortType sortType;
    private String userWhere;
    private int filter;
    private boolean nsfw;
    private List<ReadPost> readPostList;
    private List<SubredditFilter> subredditFilterList;

    private PostDataSource postDataSource;
    private MutableLiveData<PostDataSource> postDataSourceLiveData;

    PostDataSourceFactory(Retrofit retrofit, String accessToken, String accountName, Locale locale,
                          SharedPreferences sharedPreferences,
                          SharedPreferences postFeedScrolledPositionSharedPreferences, int postType,
                          SortType sortType, int filter, boolean nsfw, List<ReadPost> readPostList) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.locale = locale;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        this.filter = filter;
        this.nsfw = nsfw;
        this.readPostList = readPostList;
    }

    PostDataSourceFactory(Retrofit retrofit, String accessToken, String accountName, Locale locale,
                          SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                          String subredditName, int postType, SortType sortType, int filter, boolean nsfw,
                          List<ReadPost> readPostList, List<SubredditFilter> subredditFilterList) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.locale = locale;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.subredditName = subredditName;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        this.filter = filter;
        this.nsfw = nsfw;
        this.readPostList = readPostList;
        this.subredditFilterList = subredditFilterList;
    }

    PostDataSourceFactory(Retrofit retrofit, String accessToken, String accountName, Locale locale,
                          SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                          String subredditName, int postType, SortType sortType, String where, int filter,
                          boolean nsfw, List<ReadPost> readPostList) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.locale = locale;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.subredditName = subredditName;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        userWhere = where;
        this.filter = filter;
        this.nsfw = nsfw;
        this.readPostList = readPostList;
    }

    PostDataSourceFactory(Retrofit retrofit, String accessToken, String accountName, Locale locale,
                          SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                          String subredditName, String query, int postType, SortType sortType, int filter,
                          boolean nsfw, List<ReadPost> readPostList) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.locale = locale;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.subredditName = subredditName;
        this.query = query;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        this.filter = filter;
        this.nsfw = nsfw;
        this.readPostList = readPostList;
    }

    @NonNull
    @Override
    public DataSource<String, Post> create() {
        if (postType == PostDataSource.TYPE_FRONT_PAGE) {
            postDataSource = new PostDataSource(retrofit, accessToken, accountName, locale,
                    sharedPreferences, postFeedScrolledPositionSharedPreferences, postType, sortType, filter,
                    nsfw, readPostList);
        } else if (postType == PostDataSource.TYPE_SEARCH) {
            postDataSource = new PostDataSource(retrofit, accessToken, accountName, locale,
                    sharedPreferences, postFeedScrolledPositionSharedPreferences, subredditName, query,
                    postType, sortType, filter, nsfw, readPostList);
        } else if (postType == PostDataSource.TYPE_SUBREDDIT || postType == PostDataSource.TYPE_MULTI_REDDIT) {
            postDataSource = new PostDataSource(retrofit, accessToken, accountName, locale,
                    sharedPreferences, postFeedScrolledPositionSharedPreferences, subredditName, postType,
                    sortType, filter, nsfw, readPostList, subredditFilterList);
        } else {
            postDataSource = new PostDataSource(retrofit, accessToken, accountName, locale,
                    sharedPreferences, postFeedScrolledPositionSharedPreferences, subredditName, postType,
                    sortType, userWhere, filter, nsfw, readPostList);
        }

        postDataSourceLiveData.postValue(postDataSource);
        return postDataSource;
    }

    public MutableLiveData<PostDataSource> getPostDataSourceLiveData() {
        return postDataSourceLiveData;
    }

    PostDataSource getPostDataSource() {
        return postDataSource;
    }

    void changeNSFWAndSortType(boolean nsfw, SortType sortType) {
        this.nsfw = nsfw;
        this.sortType = sortType;
    }
}
