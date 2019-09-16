package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import java.util.Locale;
import retrofit2.Retrofit;

public class PostViewModel extends ViewModel {

  private final PostDataSourceFactory postDataSourceFactory;
  private final LiveData<NetworkState> paginationNetworkState;
  private final LiveData<NetworkState> initialLoadingState;
  private final LiveData<Boolean> hasPostLiveData;
  private final LiveData<PagedList<Post>> posts;
  private final MutableLiveData<Boolean> nsfwLiveData;
  private final MutableLiveData<String> sortTypeLiveData;
  private final NSFWAndSortTypeLiveData nsfwAndSortTypeLiveData;

  public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, int postType,
      String sortType,
      int filter, boolean nsfw) {
    postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, postType,
        sortType, filter, nsfw);

    initialLoadingState = Transformations
        .switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
            PostDataSource::getInitialLoadStateLiveData);
    paginationNetworkState = Transformations
        .switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
            PostDataSource::getPaginationNetworkStateLiveData);
    hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
        PostDataSource::hasPostLiveData);

    nsfwLiveData = new MutableLiveData<>();
    nsfwLiveData.postValue(nsfw);
    sortTypeLiveData = new MutableLiveData<>();
    sortTypeLiveData.postValue(sortType);

    nsfwAndSortTypeLiveData = new NSFWAndSortTypeLiveData(nsfwLiveData, sortTypeLiveData);

    PagedList.Config pagedListConfig =
        (new PagedList.Config.Builder())
            .setEnablePlaceholders(false)
            .setPageSize(25)
            .build();

    posts = Transformations.switchMap(nsfwAndSortTypeLiveData, nsfwAndSort -> {
      postDataSourceFactory
          .changeNSFWAndSortType(nsfwLiveData.getValue(), sortTypeLiveData.getValue());
      //noinspection unchecked
      return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
    });
  }

  public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, String subredditName,
      int postType,
      String sortType, int filter, boolean nsfw) {
    postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, subredditName,
        postType, sortType, filter, nsfw);

    initialLoadingState = Transformations
        .switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
            PostDataSource::getInitialLoadStateLiveData);
    paginationNetworkState = Transformations
        .switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
            PostDataSource::getPaginationNetworkStateLiveData);
    hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
        PostDataSource::hasPostLiveData);

    nsfwLiveData = new MutableLiveData<>();
    nsfwLiveData.postValue(nsfw);
    sortTypeLiveData = new MutableLiveData<>();
    sortTypeLiveData.postValue(sortType);

    nsfwAndSortTypeLiveData = new NSFWAndSortTypeLiveData(nsfwLiveData, sortTypeLiveData);

    PagedList.Config pagedListConfig =
        (new PagedList.Config.Builder())
            .setEnablePlaceholders(false)
            .setPageSize(25)
            .build();

    posts = Transformations.switchMap(nsfwAndSortTypeLiveData, nsfwAndSort -> {
      postDataSourceFactory
          .changeNSFWAndSortType(nsfwLiveData.getValue(), sortTypeLiveData.getValue());
      return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
    });
  }

  public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, String subredditName,
      int postType,
      String sortType, String where, int filter, boolean nsfw) {
    postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, subredditName,
        postType, sortType, where, filter, nsfw);

    initialLoadingState = Transformations
        .switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
            PostDataSource::getInitialLoadStateLiveData);
    paginationNetworkState = Transformations
        .switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
            PostDataSource::getPaginationNetworkStateLiveData);
    hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
        PostDataSource::hasPostLiveData);

    nsfwLiveData = new MutableLiveData<>();
    nsfwLiveData.postValue(nsfw);
    sortTypeLiveData = new MutableLiveData<>();
    sortTypeLiveData.postValue(sortType);

    nsfwAndSortTypeLiveData = new NSFWAndSortTypeLiveData(nsfwLiveData, sortTypeLiveData);

    PagedList.Config pagedListConfig =
        (new PagedList.Config.Builder())
            .setEnablePlaceholders(false)
            .setPageSize(25)
            .build();

    posts = Transformations.switchMap(nsfwAndSortTypeLiveData, nsfwAndSort -> {
      postDataSourceFactory
          .changeNSFWAndSortType(nsfwLiveData.getValue(), sortTypeLiveData.getValue());
      return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
    });
  }

  public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, String subredditName,
      String query,
      int postType, String sortType, int filter, boolean nsfw) {
    postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, subredditName,
        query, postType, sortType, filter, nsfw);

    initialLoadingState = Transformations
        .switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
            PostDataSource::getInitialLoadStateLiveData);
    paginationNetworkState = Transformations
        .switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
            PostDataSource::getPaginationNetworkStateLiveData);
    hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
        PostDataSource::hasPostLiveData);

    nsfwLiveData = new MutableLiveData<>();
    nsfwLiveData.postValue(nsfw);
    sortTypeLiveData = new MutableLiveData<>();
    sortTypeLiveData.postValue(sortType);

    nsfwAndSortTypeLiveData = new NSFWAndSortTypeLiveData(nsfwLiveData, sortTypeLiveData);

    PagedList.Config pagedListConfig =
        (new PagedList.Config.Builder())
            .setEnablePlaceholders(false)
            .setPageSize(25)
            .build();

    posts = Transformations.switchMap(nsfwAndSortTypeLiveData, nsfwAndSort -> {
      postDataSourceFactory
          .changeNSFWAndSortType(nsfwLiveData.getValue(), sortTypeLiveData.getValue());
      return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
    });
  }

  LiveData<PagedList<Post>> getPosts() {
    return posts;
  }

  LiveData<NetworkState> getPaginationNetworkState() {
    return paginationNetworkState;
  }

  LiveData<NetworkState> getInitialLoadingState() {
    return initialLoadingState;
  }

  LiveData<Boolean> hasPost() {
    return hasPostLiveData;
  }

  void refresh() {
    postDataSourceFactory.getPostDataSource().invalidate();
  }

  void retryLoadingMore() {
    postDataSourceFactory.getPostDataSource().retryLoadingMore();
  }

  void changeSortType(String sortType) {
    sortTypeLiveData.postValue(sortType);
  }

  void changeNSFW(boolean nsfw) {
    nsfwLiveData.postValue(nsfw);
  }

  public static class Factory extends ViewModelProvider.NewInstanceFactory {

    private final Retrofit retrofit;
    private final String accessToken;
    private final Locale locale;
    private final int postType;
    private final String sortType;
    private final int filter;
    private final boolean nsfw;
    private String subredditName;
    private String query;
    private String userWhere;

    public Factory(Retrofit retrofit, String accessToken, Locale locale, int postType,
        String sortType,
        int filter, boolean nsfw) {
      this.retrofit = retrofit;
      this.accessToken = accessToken;
      this.locale = locale;
      this.postType = postType;
      this.sortType = sortType;
      this.filter = filter;
      this.nsfw = nsfw;
    }

    public Factory(Retrofit retrofit, String accessToken, Locale locale, String subredditName,
        int postType,
        String sortType, int filter, boolean nsfw) {
      this.retrofit = retrofit;
      this.accessToken = accessToken;
      this.locale = locale;
      this.subredditName = subredditName;
      this.postType = postType;
      this.sortType = sortType;
      this.filter = filter;
      this.nsfw = nsfw;
    }

    public Factory(Retrofit retrofit, String accessToken, Locale locale, String subredditName,
        int postType,
        String sortType, String where, int filter, boolean nsfw) {
      this.retrofit = retrofit;
      this.accessToken = accessToken;
      this.locale = locale;
      this.subredditName = subredditName;
      this.postType = postType;
      this.sortType = sortType;
      userWhere = where;
      this.filter = filter;
      this.nsfw = nsfw;
    }

    public Factory(Retrofit retrofit, String accessToken, Locale locale, String subredditName,
        String query,
        int postType, String sortType, int filter, boolean nsfw) {
      this.retrofit = retrofit;
      this.accessToken = accessToken;
      this.locale = locale;
      this.subredditName = subredditName;
      this.query = query;
      this.postType = postType;
      this.sortType = sortType;
      this.filter = filter;
      this.nsfw = nsfw;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      if (postType == PostDataSource.TYPE_FRONT_PAGE) {
        return (T) new PostViewModel(retrofit, accessToken, locale, postType, sortType, filter,
            nsfw);
      } else if (postType == PostDataSource.TYPE_SEARCH) {
        return (T) new PostViewModel(retrofit, accessToken, locale, subredditName, query,
            postType, sortType, filter, nsfw);
      } else if (postType == PostDataSource.TYPE_SUBREDDIT) {
        return (T) new PostViewModel(retrofit, accessToken, locale, subredditName, postType,
            sortType, filter, nsfw);
      } else {
        return (T) new PostViewModel(retrofit, accessToken, locale, subredditName, postType,
            sortType, userWhere, filter, nsfw);
      }
    }
  }

  private static class NSFWAndSortTypeLiveData extends MediatorLiveData<Pair<Boolean, String>> {

    public NSFWAndSortTypeLiveData(LiveData<Boolean> nsfw, LiveData<String> sortType) {
      addSource(nsfw, accessToken1 -> setValue(Pair.create(accessToken1, sortType.getValue())));
      addSource(sortType, sortType1 -> setValue(Pair.create(nsfw.getValue(), sortType1)));
    }
  }
}
