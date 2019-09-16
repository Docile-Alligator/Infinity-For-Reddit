package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import java.util.Locale;
import retrofit2.Retrofit;

class PostDataSourceFactory extends DataSource.Factory {

  private final Retrofit retrofit;
  private final String accessToken;
  private final Locale locale;
  private final int postType;
  private final int filter;
  private final MutableLiveData<PostDataSource> postDataSourceLiveData;
  private String subredditName;
  private String query;
  private String sortType;
  private String userWhere;
  private boolean nsfw;
  private PostDataSource postDataSource;

  PostDataSourceFactory(Retrofit retrofit, String accessToken, Locale locale, int postType,
      String sortType,
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
      int postType, String sortType, int filter, boolean nsfw) {
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
      int postType, String sortType, String where, int filter, boolean nsfw) {
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
      String query, int postType, String sortType, int filter, boolean nsfw) {
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

  void changeSortType(String sortType) {
    this.sortType = sortType;
  }

  void changeNSFWAndSortType(boolean nsfw, String sortType) {
    this.nsfw = nsfw;
    this.sortType = sortType;
  }
}
