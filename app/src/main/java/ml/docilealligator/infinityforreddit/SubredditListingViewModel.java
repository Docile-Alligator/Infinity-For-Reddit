package ml.docilealligator.infinityforreddit;

import SubredditDatabase.SubredditData;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import retrofit2.Retrofit;

public class SubredditListingViewModel extends ViewModel {

  private final SubredditListingDataSourceFactory subredditListingDataSourceFactory;
  private final LiveData<NetworkState> paginationNetworkState;
  private final LiveData<NetworkState> initialLoadingState;
  private final LiveData<Boolean> hasSubredditLiveData;
  private final LiveData<PagedList<SubredditData>> subreddits;
  private final MutableLiveData<String> sortTypeLiveData;

  public SubredditListingViewModel(Retrofit retrofit, String query, String sortType) {
    subredditListingDataSourceFactory = new SubredditListingDataSourceFactory(retrofit, query,
        sortType);

    initialLoadingState = Transformations
        .switchMap(subredditListingDataSourceFactory.getSubredditListingDataSourceMutableLiveData(),
            SubredditListingDataSource::getInitialLoadStateLiveData);
    paginationNetworkState = Transformations
        .switchMap(subredditListingDataSourceFactory.getSubredditListingDataSourceMutableLiveData(),
            SubredditListingDataSource::getPaginationNetworkStateLiveData);
    hasSubredditLiveData = Transformations
        .switchMap(subredditListingDataSourceFactory.getSubredditListingDataSourceMutableLiveData(),
            SubredditListingDataSource::hasSubredditLiveData);

    sortTypeLiveData = new MutableLiveData<>();
    sortTypeLiveData.postValue(sortType);

    PagedList.Config pagedListConfig =
        (new PagedList.Config.Builder())
            .setEnablePlaceholders(false)
            .setPageSize(25)
            .build();

    subreddits = Transformations.switchMap(sortTypeLiveData, sort -> {
      subredditListingDataSourceFactory.changeSortType(sortTypeLiveData.getValue());
      return new LivePagedListBuilder(subredditListingDataSourceFactory, pagedListConfig).build();
    });
  }

  LiveData<PagedList<SubredditData>> getSubreddits() {
    return subreddits;
  }

  LiveData<NetworkState> getPaginationNetworkState() {
    return paginationNetworkState;
  }

  LiveData<NetworkState> getInitialLoadingState() {
    return initialLoadingState;
  }

  LiveData<Boolean> hasSubredditLiveData() {
    return hasSubredditLiveData;
  }

  void refresh() {
    subredditListingDataSourceFactory.getSubredditListingDataSource().invalidate();
  }

  void retryLoadingMore() {
    subredditListingDataSourceFactory.getSubredditListingDataSource().retryLoadingMore();
  }

  void changeSortType(String sortType) {
    sortTypeLiveData.postValue(sortType);
  }

  public static class Factory extends ViewModelProvider.NewInstanceFactory {

    private final Retrofit retrofit;
    private final String query;
    private final String sortType;

    public Factory(Retrofit retrofit, String query, String sortType) {
      this.retrofit = retrofit;
      this.query = query;
      this.sortType = sortType;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T) new SubredditListingViewModel(retrofit, query, sortType);
    }
  }
}
