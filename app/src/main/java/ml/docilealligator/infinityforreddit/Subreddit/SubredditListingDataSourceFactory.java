package ml.docilealligator.infinityforreddit.Subreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import ml.docilealligator.infinityforreddit.SortType;
import retrofit2.Retrofit;

public class SubredditListingDataSourceFactory extends DataSource.Factory {
    private Retrofit retrofit;
    private String query;
    private SortType sortType;
    private String accessToken;

    private SubredditListingDataSource subredditListingDataSource;
    private MutableLiveData<SubredditListingDataSource> subredditListingDataSourceMutableLiveData;

    SubredditListingDataSourceFactory(Retrofit retrofit, String query, SortType sortType, String accessToken) {
        this.retrofit = retrofit;
        this.query = query;
        this.sortType = sortType;
        this.accessToken = accessToken;
        subredditListingDataSourceMutableLiveData = new MutableLiveData<>();
    }

    @NonNull
    @Override
    public DataSource create() {
        subredditListingDataSource = new SubredditListingDataSource(retrofit, query, sortType, accessToken);
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
