package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import retrofit2.Retrofit;

public class SubredditListingDataSourceFactory extends DataSource.Factory {
    private Retrofit retrofit;
    private String query;
    private SubredditListingDataSource.OnSubredditListingDataFetchedCallback onSubredditListingDataFetchedCallback;

    private SubredditListingDataSource subredditListingDataSource;
    private MutableLiveData<SubredditListingDataSource> subredditListingDataSourceMutableLiveData;

    SubredditListingDataSourceFactory(Retrofit retrofit, String query,
                                      SubredditListingDataSource.OnSubredditListingDataFetchedCallback onSubredditListingDataFetchedCallback) {
        this.retrofit = retrofit;
        this.query = query;
        this.onSubredditListingDataFetchedCallback = onSubredditListingDataFetchedCallback;
        subredditListingDataSourceMutableLiveData = new MutableLiveData<>();
    }

    @NonNull
    @Override
    public DataSource create() {
        subredditListingDataSource = new SubredditListingDataSource(retrofit,
                query, onSubredditListingDataFetchedCallback);
        subredditListingDataSourceMutableLiveData.postValue(subredditListingDataSource);
        return subredditListingDataSource;
    }

    public MutableLiveData<SubredditListingDataSource> getSubredditListingDataSourceMutableLiveData() {
        return subredditListingDataSourceMutableLiveData;
    }

    SubredditListingDataSource getSubredditListingDataSource() {
        return subredditListingDataSource;
    }
}
