package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import retrofit2.Retrofit;

public class SubredditListingDataSourceFactory extends DataSource.Factory {

  private final Retrofit retrofit;
  private final String query;
  private final MutableLiveData<SubredditListingDataSource> subredditListingDataSourceMutableLiveData;
  private String sortType;
  private SubredditListingDataSource subredditListingDataSource;

  SubredditListingDataSourceFactory(Retrofit retrofit, String query, String sortType) {
    this.retrofit = retrofit;
    this.query = query;
    this.sortType = sortType;
    subredditListingDataSourceMutableLiveData = new MutableLiveData<>();
  }

  @NonNull
  @Override
  public DataSource create() {
    subredditListingDataSource = new SubredditListingDataSource(retrofit, query, sortType);
    subredditListingDataSourceMutableLiveData.postValue(subredditListingDataSource);
    return subredditListingDataSource;
  }

  public MutableLiveData<SubredditListingDataSource> getSubredditListingDataSourceMutableLiveData() {
    return subredditListingDataSourceMutableLiveData;
  }

  SubredditListingDataSource getSubredditListingDataSource() {
    return subredditListingDataSource;
  }

  void changeSortType(String sortType) {
    this.sortType = sortType;
  }
}
