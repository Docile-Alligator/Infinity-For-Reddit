package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import retrofit2.Retrofit;

public class UserListingDataSourceFactory extends DataSource.Factory {

  private final Retrofit retrofit;
  private final String query;
  private final MutableLiveData<UserListingDataSource> userListingDataSourceMutableLiveData;
  private String sortType;
  private UserListingDataSource userListingDataSource;

  UserListingDataSourceFactory(Retrofit retrofit, String query, String sortType) {
    this.retrofit = retrofit;
    this.query = query;
    this.sortType = sortType;
    userListingDataSourceMutableLiveData = new MutableLiveData<>();
  }

  @NonNull
  @Override
  public DataSource create() {
    userListingDataSource = new UserListingDataSource(retrofit, query, sortType);
    userListingDataSourceMutableLiveData.postValue(userListingDataSource);
    return userListingDataSource;
  }

  public MutableLiveData<UserListingDataSource> getUserListingDataSourceMutableLiveData() {
    return userListingDataSourceMutableLiveData;
  }

  UserListingDataSource getUserListingDataSource() {
    return userListingDataSource;
  }

  void changeSortType(String sortType) {
    this.sortType = sortType;
  }
}
