package ml.docilealligator.infinityforreddit;

import User.UserData;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import retrofit2.Retrofit;

public class UserListingViewModel extends ViewModel {

  private final UserListingDataSourceFactory userListingDataSourceFactory;
  private final LiveData<NetworkState> paginationNetworkState;
  private final LiveData<NetworkState> initialLoadingState;
  private final LiveData<Boolean> hasUserLiveData;
  private final LiveData<PagedList<UserData>> users;
  private final MutableLiveData<String> sortTypeLiveData;

  public UserListingViewModel(Retrofit retrofit, String query, String sortType) {
    userListingDataSourceFactory = new UserListingDataSourceFactory(retrofit, query, sortType);

    initialLoadingState = Transformations
        .switchMap(userListingDataSourceFactory.getUserListingDataSourceMutableLiveData(),
            UserListingDataSource::getInitialLoadStateLiveData);
    paginationNetworkState = Transformations
        .switchMap(userListingDataSourceFactory.getUserListingDataSourceMutableLiveData(),
            UserListingDataSource::getPaginationNetworkStateLiveData);
    hasUserLiveData = Transformations
        .switchMap(userListingDataSourceFactory.getUserListingDataSourceMutableLiveData(),
            UserListingDataSource::hasUserLiveData);

    sortTypeLiveData = new MutableLiveData<>();
    sortTypeLiveData.postValue(sortType);

    PagedList.Config pagedListConfig =
        (new PagedList.Config.Builder())
            .setEnablePlaceholders(false)
            .setPageSize(25)
            .build();

    users = Transformations.switchMap(sortTypeLiveData, sort -> {
      userListingDataSourceFactory.changeSortType(sortTypeLiveData.getValue());
      return (new LivePagedListBuilder(userListingDataSourceFactory, pagedListConfig)).build();
    });
  }

  LiveData<PagedList<UserData>> getUsers() {
    return users;
  }

  LiveData<NetworkState> getPaginationNetworkState() {
    return paginationNetworkState;
  }

  LiveData<NetworkState> getInitialLoadingState() {
    return initialLoadingState;
  }

  LiveData<Boolean> hasUser() {
    return hasUserLiveData;
  }

  void refresh() {
    userListingDataSourceFactory.getUserListingDataSource().invalidate();
  }

  void retryLoadingMore() {
    userListingDataSourceFactory.getUserListingDataSource().retryLoadingMore();
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
      return (T) new UserListingViewModel(retrofit, query, sortType);
    }
  }
}
