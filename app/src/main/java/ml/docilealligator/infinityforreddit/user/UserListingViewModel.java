package ml.docilealligator.infinityforreddit.user;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.SortType;
import retrofit2.Retrofit;

public class UserListingViewModel extends ViewModel {
    private UserListingDataSourceFactory userListingDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<Boolean> hasUserLiveData;
    private LiveData<PagedList<UserData>> users;
    private MutableLiveData<SortType> sortTypeLiveData;

    public UserListingViewModel(Retrofit retrofit, String query, SortType sortType, boolean nsfw) {
        userListingDataSourceFactory = new UserListingDataSourceFactory(retrofit, query, sortType, nsfw);

        initialLoadingState = Transformations.switchMap(userListingDataSourceFactory.getUserListingDataSourceMutableLiveData(),
                UserListingDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(userListingDataSourceFactory.getUserListingDataSourceMutableLiveData(),
                UserListingDataSource::getPaginationNetworkStateLiveData);
        hasUserLiveData = Transformations.switchMap(userListingDataSourceFactory.getUserListingDataSourceMutableLiveData(),
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

    public LiveData<PagedList<UserData>> getUsers() {
        return users;
    }

    public LiveData<NetworkState> getPaginationNetworkState() {
        return paginationNetworkState;
    }

    public LiveData<NetworkState> getInitialLoadingState() {
        return initialLoadingState;
    }

    public LiveData<Boolean> hasUser() {
        return hasUserLiveData;
    }

    public void refresh() {
        userListingDataSourceFactory.getUserListingDataSource().invalidate();
    }

    public void retryLoadingMore() {
        userListingDataSourceFactory.getUserListingDataSource().retryLoadingMore();
    }

    public void changeSortType(SortType sortType) {
        sortTypeLiveData.postValue(sortType);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Retrofit retrofit;
        private String query;
        private SortType sortType;
        private boolean nsfw;

        public Factory(Retrofit retrofit, String query, SortType sortType, boolean nsfw) {
            this.retrofit = retrofit;
            this.query = query;
            this.sortType = sortType;
            this.nsfw = nsfw;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new UserListingViewModel(retrofit, query, sortType, nsfw);
        }
    }
}
