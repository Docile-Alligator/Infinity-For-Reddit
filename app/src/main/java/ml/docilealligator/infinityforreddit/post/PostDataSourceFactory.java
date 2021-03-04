package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import java.util.List;

import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import retrofit2.Retrofit;

class PostDataSourceFactory extends DataSource.Factory {
    private Retrofit retrofit;
    private String accessToken;
    private String accountName;
    private SharedPreferences sharedPreferences;
    private SharedPreferences postFeedScrolledPositionSharedPreferences;
    private String name;
    private String query;
    private int postType;
    private SortType sortType;
    private PostFilter postFilter;
    private String userWhere;
    private List<ReadPost> readPostList;

    private PostDataSource postDataSource;
    private MutableLiveData<PostDataSource> postDataSourceLiveData;

    PostDataSourceFactory(Retrofit retrofit, String accessToken, String accountName,
                          SharedPreferences sharedPreferences,
                          SharedPreferences postFeedScrolledPositionSharedPreferences, int postType,
                          SortType sortType, PostFilter postFilter, List<ReadPost> readPostList) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        this.postFilter = postFilter;
        this.readPostList = readPostList;
    }

    PostDataSourceFactory(Retrofit retrofit, String accessToken, String accountName,
                          SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                          String name, int postType, SortType sortType, PostFilter postFilter,
                          List<ReadPost> readPostList) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.name = name;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        this.postFilter = postFilter;
        this.readPostList = readPostList;
    }

    PostDataSourceFactory(Retrofit retrofit, String accessToken, String accountName,
                          SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                          String name, int postType, SortType sortType, PostFilter postFilter,
                          String where, List<ReadPost> readPostList) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.name = name;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        this.postFilter = postFilter;
        userWhere = where;
        this.readPostList = readPostList;
    }

    PostDataSourceFactory(Retrofit retrofit, String accessToken, String accountName,
                          SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                          String name, String query, int postType, SortType sortType, PostFilter postFilter,
                          List<ReadPost> readPostList) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.name = name;
        this.query = query;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        this.postFilter = postFilter;
        this.readPostList = readPostList;
    }

    @NonNull
    @Override
    public DataSource<String, Post> create() {
        if (postType == PostDataSource.TYPE_FRONT_PAGE) {
            postDataSource = new PostDataSource(retrofit, accessToken, accountName,
                    sharedPreferences, postFeedScrolledPositionSharedPreferences, postType, sortType,
                    postFilter, readPostList);
        } else if (postType == PostDataSource.TYPE_SEARCH) {
            postDataSource = new PostDataSource(retrofit, accessToken, accountName,
                    sharedPreferences, postFeedScrolledPositionSharedPreferences, name, query,
                    postType, sortType, postFilter, readPostList);
        } else if (postType == PostDataSource.TYPE_SUBREDDIT || postType == PostDataSource.TYPE_MULTI_REDDIT) {
            Log.i("asdasfd", "s5 " + (postFilter == null));
            postDataSource = new PostDataSource(retrofit, accessToken, accountName,
                    sharedPreferences, postFeedScrolledPositionSharedPreferences, name, postType,
                    sortType, postFilter, readPostList);
        } else if (postType == PostDataSource.TYPE_ANONYMOUS_FRONT_PAGE) {
            postDataSource = new PostDataSource(retrofit, null, null,
                    sharedPreferences, null, name, postType,
                    sortType, postFilter, null);
        } else {
            postDataSource = new PostDataSource(retrofit, accessToken, accountName,
                    sharedPreferences, postFeedScrolledPositionSharedPreferences, name, postType,
                    sortType, postFilter, userWhere, readPostList);
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

    void changeSortTypeAndPostFilter(SortType sortType, PostFilter postFilter) {
        Log.i("asdasfd", "s6 " + (postFilter == null));
        this.sortType = sortType;
        this.postFilter = postFilter;
    }
}
