package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import SubredditDatabase.SubredditData;
import retrofit2.Retrofit;

public class SubredditListingViewModel extends ViewModel {
    private SubredditListingDataSourceFactory subredditListingDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<PagedList<SubredditData>> subreddits;
    private MutableLiveData<String> sortTypeLiveData;

    SubredditListingViewModel(Retrofit retrofit, String query, String sortType,
                              SubredditListingDataSource.OnSubredditListingDataFetchedCallback onSubredditListingDataFetchedCallback) {
        subredditListingDataSourceFactory = new SubredditListingDataSourceFactory(retrofit, query, sortType, onSubredditListingDataFetchedCallback);

        initialLoadingState = Transformations.switchMap(subredditListingDataSourceFactory.getSubredditListingDataSourceMutableLiveData(),
                (Function<SubredditListingDataSource, LiveData<NetworkState>>) SubredditListingDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(subredditListingDataSourceFactory.getSubredditListingDataSourceMutableLiveData(),
                (Function<SubredditListingDataSource, LiveData<NetworkState>>) SubredditListingDataSource::getPaginationNetworkStateLiveData);

        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        subreddits = Transformations.switchMap(sortTypeLiveData, sort -> {
            subredditListingDataSourceFactory.changeSortType(sortTypeLiveData.getValue());
            return new LivePagedListBuilder(subredditListingDataSourceFactory, pagedListConfig).build();
        });
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

    void changeSortType(String sortType) {
        sortTypeLiveData.postValue(sortType);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Retrofit retrofit;
        private String query;
        private String sortType;
        private SubredditListingDataSource.OnSubredditListingDataFetchedCallback onSubredditListingDataFetchedCallback;

        public Factory(Retrofit retrofit, String query, String sortType,
                       SubredditListingDataSource.OnSubredditListingDataFetchedCallback onSubredditListingDataFetchedCallback) {
            this.retrofit = retrofit;
            this.query = query;
            this.sortType = sortType;
            this.onSubredditListingDataFetchedCallback = onSubredditListingDataFetchedCallback;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SubredditListingViewModel(retrofit, query, sortType, onSubredditListingDataFetchedCallback);
        }
    }
}
