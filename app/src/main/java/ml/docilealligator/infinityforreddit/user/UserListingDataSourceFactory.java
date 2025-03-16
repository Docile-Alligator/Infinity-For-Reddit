package ml.docilealligator.infinityforreddit.user;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import ml.docilealligator.infinityforreddit.thing.SortType;
import retrofit2.Retrofit;

public class UserListingDataSourceFactory extends DataSource.Factory {
    private final Retrofit retrofit;
    private final String query;
    private SortType sortType;
    private final boolean nsfw;

    private UserListingDataSource userListingDataSource;
    private final MutableLiveData<UserListingDataSource> userListingDataSourceMutableLiveData;

    UserListingDataSourceFactory(Retrofit retrofit, String query, SortType sortType, boolean nsfw) {
        this.retrofit = retrofit;
        this.query = query;
        this.sortType = sortType;
        this.nsfw = nsfw;
        userListingDataSourceMutableLiveData = new MutableLiveData<>();
    }

    @NonNull
    @Override
    public DataSource create() {
        userListingDataSource = new UserListingDataSource(retrofit, query, sortType, nsfw);
        userListingDataSourceMutableLiveData.postValue(userListingDataSource);
        return userListingDataSource;
    }

    public MutableLiveData<UserListingDataSource> getUserListingDataSourceMutableLiveData() {
        return userListingDataSourceMutableLiveData;
    }

    UserListingDataSource getUserListingDataSource() {
        return userListingDataSource;
    }

    void changeSortType(SortType sortType) {
        this.sortType = sortType;
    }
}
