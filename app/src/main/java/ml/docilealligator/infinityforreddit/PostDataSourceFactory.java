package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import java.util.Locale;

import retrofit2.Retrofit;

class PostDataSourceFactory extends DataSource.Factory {
    private Retrofit retrofit;
    private String accessToken;
    private Locale locale;
    private String subredditName;
    private String query;
    private int postType;
    private SortType sortType;
    private String userWhere;
    private int filter;
    private boolean nsfw;

    private PostDataSource postDataSource;
    private MutableLiveData<PostDataSource> postDataSourceLiveData;

    PostDataSourceFactory(Retrofit retrofit, String accessToken, Locale locale, int postType, SortType sortType,
                          int filter, boolean nsfw) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        this.filter = filter;
        this.nsfw = nsfw;
    }

    PostDataSourceFactory(Retrofit retrofit, String accessToken, Locale locale, String subredditName,
                          int postType, SortType sortType, int filter, boolean nsfw) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.subredditName = subredditName;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        this.filter = filter;
        this.nsfw = nsfw;
    }

    PostDataSourceFactory(Retrofit retrofit, String accessToken, Locale locale, String subredditName,
                          int postType, SortType sortType, String where, int filter, boolean nsfw) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.subredditName = subredditName;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        userWhere = where;
        this.filter = filter;
        this.nsfw = nsfw;
    }

    PostDataSourceFactory(Retrofit retrofit, String accessToken, Locale locale, String subredditName,
                          String query, int postType, SortType sortType, int filter, boolean nsfw) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.subredditName = subredditName;
        this.query = query;
        postDataSourceLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        this.filter = filter;
        this.nsfw = nsfw;
    }

    @NonNull
    @Override
    public DataSource<String, Post> create() {
        if (postType == PostDataSource.TYPE_FRONT_PAGE) {
            postDataSource = new PostDataSource(retrofit, accessToken, locale, postType, sortType,
                    filter, nsfw);
        } else if (postType == PostDataSource.TYPE_SEARCH) {
            postDataSource = new PostDataSource(retrofit, accessToken, locale, subredditName, query,
                    postType, sortType, filter, nsfw);
        } else if (postType == PostDataSource.TYPE_SUBREDDIT) {
            postDataSource = new PostDataSource(retrofit, accessToken, locale, subredditName, postType,
                    sortType, filter, nsfw);
        } else {
            postDataSource = new PostDataSource(retrofit, accessToken, locale, subredditName, postType,
                    sortType, userWhere, filter, nsfw);
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

    void changeSortType(SortType sortType) {
        this.sortType = sortType;
    }

    void changeNSFWAndSortType(boolean nsfw, SortType sortType) {
        this.nsfw = nsfw;
        this.sortType = sortType;
    }
}
