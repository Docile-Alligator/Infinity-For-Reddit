package ml.docilealligator.infinityforreddit.subreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.SortType;
import retrofit2.Retrofit;

public class SubredditListingDataSource extends PageKeyedDataSource<String, SubredditData> {

    private Retrofit retrofit;
    private String query;
    private SortType sortType;
    private String accessToken;
    private boolean nsfw;

    private MutableLiveData<NetworkState> paginationNetworkStateLiveData;
    private MutableLiveData<NetworkState> initialLoadStateLiveData;
    private MutableLiveData<Boolean> hasSubredditLiveData;

    private LoadParams<String> params;
    private LoadCallback<String, SubredditData> callback;

    SubredditListingDataSource(Retrofit retrofit, String query, SortType sortType, String accessToken, boolean nsfw) {
        this.retrofit = retrofit;
        this.query = query;
        this.sortType = sortType;
        this.accessToken = accessToken;
        this.nsfw = nsfw;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasSubredditLiveData = new MutableLiveData<>();
    }

    MutableLiveData<NetworkState> getPaginationNetworkStateLiveData() {
        return paginationNetworkStateLiveData;
    }

    MutableLiveData<NetworkState> getInitialLoadStateLiveData() {
        return initialLoadStateLiveData;
    }

    MutableLiveData<Boolean> hasSubredditLiveData() {
        return hasSubredditLiveData;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull LoadInitialCallback<String, SubredditData> callback) {
        initialLoadStateLiveData.postValue(NetworkState.LOADING);

        FetchSubredditData.fetchSubredditListingData(retrofit, query, null, sortType.getType().value, accessToken, nsfw,
                new FetchSubredditData.FetchSubredditListingDataListener() {
                    @Override
                    public void onFetchSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after) {
                        if (subredditData.size() == 0) {
                            hasSubredditLiveData.postValue(false);
                        } else {
                            hasSubredditLiveData.postValue(true);
                        }

                        callback.onResult(subredditData, null, after);
                        initialLoadStateLiveData.postValue(NetworkState.LOADED);
                    }

                    @Override
                    public void onFetchSubredditListingDataFail() {
                        initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error retrieving subreddit list"));
                    }
                });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, SubredditData> callback) {

    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, SubredditData> callback) {
        this.params = params;
        this.callback = callback;

        if (params.key == null || params.key.equals("") || params.key.equals("null")) {
            return;
        }

        FetchSubredditData.fetchSubredditListingData(retrofit, query, params.key, sortType.getType().value, accessToken, nsfw,
                new FetchSubredditData.FetchSubredditListingDataListener() {
                    @Override
                    public void onFetchSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after) {
                        callback.onResult(subredditData, after);
                        paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                    }

                    @Override
                    public void onFetchSubredditListingDataFail() {
                        paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error retrieving subreddit list"));
                    }
                });
    }

    void retryLoadingMore() {
        loadAfter(params, callback);
    }
}
