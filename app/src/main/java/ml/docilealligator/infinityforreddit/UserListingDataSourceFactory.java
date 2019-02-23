package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import retrofit2.Retrofit;

public class UserListingDataSourceFactory extends DataSource.Factory {
    private Retrofit retrofit;
    private String query;
    private UserListingDataSource.OnUserListingDataFetchedCallback onUserListingDataFetchedCallback;

    private UserListingDataSource UserListingDataSource;
    private MutableLiveData<UserListingDataSource> UserListingDataSourceMutableLiveData;

    UserListingDataSourceFactory(Retrofit retrofit, String query,
                                      UserListingDataSource.OnUserListingDataFetchedCallback onUserListingDataFetchedCallback) {
        this.retrofit = retrofit;
        this.query = query;
        this.onUserListingDataFetchedCallback = onUserListingDataFetchedCallback;
        UserListingDataSourceMutableLiveData = new MutableLiveData<>();
    }

    @NonNull
    @Override
    public DataSource create() {
        UserListingDataSource UserListingDataSource = new UserListingDataSource(retrofit,
                query, onUserListingDataFetchedCallback);
        UserListingDataSourceMutableLiveData.postValue(UserListingDataSource);
        return UserListingDataSource;
    }

    public MutableLiveData<UserListingDataSource> getUserListingDataSourceMutableLiveData() {
        return UserListingDataSourceMutableLiveData;
    }

    UserListingDataSource getUserListingDataSource() {
        return UserListingDataSource;
    }
}
