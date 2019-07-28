package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import java.util.ArrayList;

import User.UserData;
import retrofit2.Retrofit;

public class UserListingDataSource extends PageKeyedDataSource<String, UserData> {
    interface OnUserListingDataFetchedCallback {
        void hasUser();
        void noUser();
    }

    private Retrofit retrofit;
    private String query;
    private String sortType;
    private UserListingDataSource.OnUserListingDataFetchedCallback onUserListingDataFetchedCallback;

    private MutableLiveData<NetworkState> paginationNetworkStateLiveData;
    private MutableLiveData<NetworkState> initialLoadStateLiveData;

    private PageKeyedDataSource.LoadInitialParams<String> initialParams;
    private PageKeyedDataSource.LoadInitialCallback<String, UserData> initialCallback;
    private PageKeyedDataSource.LoadParams<String> params;
    private PageKeyedDataSource.LoadCallback<String, UserData> callback;

    UserListingDataSource(Retrofit retrofit, String query, String sortType,
                               UserListingDataSource.OnUserListingDataFetchedCallback onUserListingDataFetchedCallback) {
        this.retrofit = retrofit;
        this.query = query;
        this.sortType = sortType;
        this.onUserListingDataFetchedCallback = onUserListingDataFetchedCallback;
        paginationNetworkStateLiveData = new MutableLiveData();
        initialLoadStateLiveData = new MutableLiveData();
    }

    MutableLiveData getPaginationNetworkStateLiveData() {
        return paginationNetworkStateLiveData;
    }

    MutableLiveData getInitialLoadStateLiveData() {
        return initialLoadStateLiveData;
    }

    @Override
    public void loadInitial(@NonNull PageKeyedDataSource.LoadInitialParams<String> params, @NonNull PageKeyedDataSource.LoadInitialCallback<String, UserData> callback) {
        initialParams = params;
        initialCallback = callback;

        initialLoadStateLiveData.postValue(NetworkState.LOADING);

        FetchUserData.fetchUserListingData(retrofit, query, null, sortType, new FetchUserData.FetchUserListingDataListener() {
            @Override
            public void onFetchUserListingDataSuccess(ArrayList<UserData> UserData, String after) {
                if(UserData.size() == 0) {
                    onUserListingDataFetchedCallback.noUser();
                } else {
                    onUserListingDataFetchedCallback.hasUser();
                }

                callback.onResult(UserData, null, after);
                initialLoadStateLiveData.postValue(NetworkState.LOADED);
            }

            @Override
            public void onFetchUserListingDataFailed() {
                initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error retrieving User list"));
            }
        });
    }

    @Override
    public void loadBefore(@NonNull PageKeyedDataSource.LoadParams<String> params, @NonNull PageKeyedDataSource.LoadCallback<String, UserData> callback) {

    }

    @Override
    public void loadAfter(@NonNull PageKeyedDataSource.LoadParams<String> params, @NonNull PageKeyedDataSource.LoadCallback<String, UserData> callback) {
        this.params = params;
        this.callback = callback;

        if(params.key.equals("null")) {
            return;
        }

        FetchUserData.fetchUserListingData(retrofit, query, params.key, sortType, new FetchUserData.FetchUserListingDataListener() {
            @Override
            public void onFetchUserListingDataSuccess(ArrayList<UserData> UserData, String after) {
                callback.onResult(UserData, after);
                paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
            }

            @Override
            public void onFetchUserListingDataFailed() {
                paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error retrieving User list"));
            }
        });
    }

    void retry() {
        loadInitial(initialParams, initialCallback);
    }

    void retryLoadingMore() {
        loadAfter(params, callback);
    }
}
