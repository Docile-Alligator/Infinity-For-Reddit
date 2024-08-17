package ml.docilealligator.infinityforreddit.subreddit;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.SortType;
import retrofit2.Retrofit;

public class SubredditListingDataSourceFactory extends DataSource.Factory {
    private final Executor executor;
    private final Handler handler;
    private final Retrofit retrofit;
    private final String query;
    private SortType sortType;
    @Nullable
    private final String accessToken;
    @NonNull
    private final String accountName;
    private final boolean nsfw;

    private SubredditListingDataSource subredditListingDataSource;
    private final MutableLiveData<SubredditListingDataSource> subredditListingDataSourceMutableLiveData;

    SubredditListingDataSourceFactory(Executor executor, Handler handler, Retrofit retrofit, String query, SortType sortType,
                                      @Nullable String accessToken, @NonNull String accountName, boolean nsfw) {
        this.executor = executor;
        this.handler = handler;
        this.retrofit = retrofit;
        this.query = query;
        this.sortType = sortType;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.nsfw = nsfw;
        subredditListingDataSourceMutableLiveData = new MutableLiveData<>();
    }

    @NonNull
    @Override
    public DataSource create() {
        subredditListingDataSource = new SubredditListingDataSource(executor, handler, retrofit, query, sortType,
                accessToken, accountName, nsfw);
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
