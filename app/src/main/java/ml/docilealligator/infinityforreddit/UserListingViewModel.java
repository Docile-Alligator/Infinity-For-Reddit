package ml.docilealligator.infinityforreddit;

import User.UserData;
import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import retrofit2.Retrofit;

public class UserListingViewModel extends ViewModel {
    private UserListingDataSourceFactory UserListingDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<PagedList<UserData>> Users;

    UserListingViewModel(Retrofit retrofit, String query,
                              UserListingDataSource.OnUserListingDataFetchedCallback onUserListingDataFetchedCallback) {
        UserListingDataSourceFactory = new UserListingDataSourceFactory(retrofit, query, onUserListingDataFetchedCallback);

        initialLoadingState = Transformations.switchMap(UserListingDataSourceFactory.getUserListingDataSourceMutableLiveData(),
                (Function<UserListingDataSource, LiveData<NetworkState>>) UserListingDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(UserListingDataSourceFactory.getUserListingDataSourceMutableLiveData(),
                (Function<UserListingDataSource, LiveData<NetworkState>>) UserListingDataSource::getPaginationNetworkStateLiveData);
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        Users = (new LivePagedListBuilder(UserListingDataSourceFactory, pagedListConfig)).build();
    }

    LiveData<PagedList<UserData>> getUsers() {
        return Users;
    }

    LiveData<NetworkState> getPaginationNetworkState() {
        return paginationNetworkState;
    }

    LiveData<NetworkState> getInitialLoadingState() {
        return initialLoadingState;
    }

    void refresh() {
        UserListingDataSourceFactory.getUserListingDataSource().invalidate();
    }

    void retry() {
        UserListingDataSourceFactory.getUserListingDataSource().retry();
    }

    void retryLoadingMore() {
        UserListingDataSourceFactory.getUserListingDataSource().retryLoadingMore();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Retrofit retrofit;
        private String query;
        private UserListingDataSource.OnUserListingDataFetchedCallback onUserListingDataFetchedCallback;

        public Factory(Retrofit retrofit, String query,
                       UserListingDataSource.OnUserListingDataFetchedCallback onUserListingDataFetchedCallback) {
            this.retrofit = retrofit;
            this.query = query;
            this.onUserListingDataFetchedCallback = onUserListingDataFetchedCallback;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new UserListingViewModel(retrofit, query, onUserListingDataFetchedCallback);
        }
    }
}
