package ml.docilealligator.infinityforreddit.user;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import ml.docilealligator.infinityforreddit.SortType;
import retrofit2.Retrofit;

public class UserListingDataSourceFactory extends DataSource.Factory {
    private final Retrofit applicationOnlyOauthRetrofit;
    private final String query;
    private SortType sortType;
    private final boolean nsfw;

    private UserListingDataSource userListingDataSource;
    private final MutableLiveData<UserListingDataSource> userListingDataSourceMutableLiveData;

    UserListingDataSourceFactory(Retrofit applicationOnlyOauthRetrofit, String query, SortType sortType, boolean nsfw) {
        this.applicationOnlyOauthRetrofit = applicationOnlyOauthRetrofit;
        this.query = query;
        this.sortType = sortType;
        this.nsfw = nsfw;
        userListingDataSourceMutableLiveData = new MutableLiveData<>();
    }

    @NonNull
    @Override
    public DataSource create() {
        userListingDataSource = new UserListingDataSource(applicationOnlyOauthRetrofit, query, sortType, nsfw);
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
