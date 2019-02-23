package ml.docilealligator.infinityforreddit;

import SubredditDatabase.SubredditData;
import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import retrofit2.Retrofit;

public class SubredditListingViewModel extends ViewModel {
    private SubredditListingDataSourceFactory subredditListingDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<PagedList<SubredditData>> subreddits;

    SubredditListingViewModel(Retrofit retrofit, String query,
                              SubredditListingDataSource.OnSubredditListingDataFetchedCallback onSubredditListingDataFetchedCallback) {
        subredditListingDataSourceFactory = new SubredditListingDataSourceFactory(retrofit, query, onSubredditListingDataFetchedCallback);

        initialLoadingState = Transformations.switchMap(subredditListingDataSourceFactory.getSubredditListingDataSourceMutableLiveData(),
                (Function<SubredditListingDataSource, LiveData<NetworkState>>) SubredditListingDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(subredditListingDataSourceFactory.getSubredditListingDataSourceMutableLiveData(),
                (Function<SubredditListingDataSource, LiveData<NetworkState>>) SubredditListingDataSource::getPaginationNetworkStateLiveData);
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        subreddits = (new LivePagedListBuilder(subredditListingDataSourceFactory, pagedListConfig)).build();
    }

    LiveData<PagedList<SubredditData>> getSubreddits() {
        return subreddits;
    }

    LiveData<NetworkState> getPaginationNetworkState() {
        return paginationNetworkState;
    }

    LiveData<NetworkState> getInitialLoadingState() {
        return initialLoadingState;
    }

    void refresh() {
        subredditListingDataSourceFactory.getSubredditListingDataSource().invalidate();
    }

    void retry() {
        subredditListingDataSourceFactory.getSubredditListingDataSource().retry();
    }

    void retryLoadingMore() {
        subredditListingDataSourceFactory.getSubredditListingDataSource().retryLoadingMore();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Retrofit retrofit;
        private String query;
        private SubredditListingDataSource.OnSubredditListingDataFetchedCallback onSubredditListingDataFetchedCallback;

        public Factory(Retrofit retrofit, String query,
                       SubredditListingDataSource.OnSubredditListingDataFetchedCallback onSubredditListingDataFetchedCallback) {
            this.retrofit = retrofit;
            this.query = query;
            this.onSubredditListingDataFetchedCallback = onSubredditListingDataFetchedCallback;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SubredditListingViewModel(retrofit, query, onSubredditListingDataFetchedCallback);
        }
    }
}
