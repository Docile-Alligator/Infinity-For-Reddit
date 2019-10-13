package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import retrofit2.Retrofit;

public class SubredditListingDataSourceFactory extends DataSource.Factory {
    private Retrofit retrofit;
    private String query;
    private SortType sortType;

    private SubredditListingDataSource subredditListingDataSource;
    private MutableLiveData<SubredditListingDataSource> subredditListingDataSourceMutableLiveData;

    SubredditListingDataSourceFactory(Retrofit retrofit, String query, SortType sortType) {
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

    void changeSortType(SortType sortType) {
        this.sortType = sortType;
    }
}
