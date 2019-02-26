package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import retrofit2.Retrofit;

public class UserListingDataSourceFactory extends DataSource.Factory {
    private Retrofit retrofit;
    private String query;
    private UserListingDataSource.OnUserListingDataFetchedCallback onUserListingDataFetchedCallback;

    private UserListingDataSource userListingDataSource;
    private MutableLiveData<UserListingDataSource> userListingDataSourceMutableLiveData;

    UserListingDataSourceFactory(Retrofit retrofit, String query,
                                      UserListingDataSource.OnUserListingDataFetchedCallback onUserListingDataFetchedCallback) {
        this.retrofit = retrofit;
        this.query = query;
        this.onUserListingDataFetchedCallback = onUserListingDataFetchedCallback;
        userListingDataSourceMutableLiveData = new MutableLiveData<>();
    }

    @NonNull
    @Override
    public DataSource create() {
        userListingDataSource = new UserListingDataSource(retrofit,
                query, onUserListingDataFetchedCallback);
        userListingDataSourceMutableLiveData.postValue(userListingDataSource);
        return userListingDataSource;
    }

    public MutableLiveData<UserListingDataSource> getUserListingDataSourceMutableLiveData() {
        return userListingDataSourceMutableLiveData;
    }

    UserListingDataSource getUserListingDataSource() {
        return userListingDataSource;
    }
}
