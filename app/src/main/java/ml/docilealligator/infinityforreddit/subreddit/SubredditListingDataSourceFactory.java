package ml.docilealligator.infinityforreddit.subreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import ml.docilealligator.infinityforreddit.SortType;
import retrofit2.Retrofit;

public class SubredditListingDataSourceFactory extends DataSource.Factory {
    private final Retrofit retrofit;
    private final String query;
    private SortType sortType;
    private final String accessToken;
    private final boolean nsfw;

    private SubredditListingDataSource subredditListingDataSource;
    private final MutableLiveData<SubredditListingDataSource> subredditListingDataSourceMutableLiveData;

    SubredditListingDataSourceFactory(Retrofit retrofit, String query, SortType sortType, String accessToken, boolean nsfw) {
        this.retrofit = retrofit;
        this.query = query;
        this.sortType = sortType;
        this.accessToken = accessToken;
        this.nsfw = nsfw;
        subredditListingDataSourceMutableLiveData = new MutableLiveData<>();
    }

    @NonNull
    @Override
    public DataSource create() {
        subredditListingDataSource = new SubredditListingDataSource(retrofit, query, sortType, accessToken, nsfw);
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
