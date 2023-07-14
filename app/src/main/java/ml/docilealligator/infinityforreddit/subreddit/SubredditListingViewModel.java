package ml.docilealligator.infinityforreddit.subreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.SortType;
import retrofit2.Retrofit;

public class SubredditListingViewModel extends ViewModel {
    private SubredditListingDataSourceFactory subredditListingDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<Boolean> hasSubredditLiveData;
    private LiveData<PagedList<SubredditData>> subreddits;
    private MutableLiveData<SortType> sortTypeLiveData;

    public SubredditListingViewModel(Retrofit retrofit, String query, SortType sortType, String accessToken, boolean nsfw) {
        subredditListingDataSourceFactory = new SubredditListingDataSourceFactory(retrofit, query, sortType, accessToken, nsfw);

        initialLoadingState = Transformations.switchMap(subredditListingDataSourceFactory.getSubredditListingDataSourceMutableLiveData(),
                SubredditListingDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(subredditListingDataSourceFactory.getSubredditListingDataSourceMutableLiveData(),
                SubredditListingDataSource::getPaginationNetworkStateLiveData);
        hasSubredditLiveData = Transformations.switchMap(subredditListingDataSourceFactory.getSubredditListingDataSourceMutableLiveData(),
                SubredditListingDataSource::hasSubredditLiveData);

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

    public LiveData<PagedList<SubredditData>> getSubreddits() {
        return subreddits;
    }

    public LiveData<NetworkState> getPaginationNetworkState() {
        return paginationNetworkState;
    }

    public LiveData<NetworkState> getInitialLoadingState() {
        return initialLoadingState;
    }

    public LiveData<Boolean> hasSubredditLiveData() {
        return hasSubredditLiveData;
    }

    public void refresh() {
        subredditListingDataSourceFactory.getSubredditListingDataSource().invalidate();
    }

    public void retryLoadingMore() {
        subredditListingDataSourceFactory.getSubredditListingDataSource().retryLoadingMore();
    }

    public void changeSortType(SortType sortType) {
        sortTypeLiveData.postValue(sortType);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Retrofit retrofit;
        private String query;
        private SortType sortType;
        private String accessToken;
        private boolean nsfw;

        public Factory(Retrofit retrofit, String query, SortType sortType, String accessToken, boolean nsfw) {
            this.retrofit = retrofit;
            this.query = query;
            this.sortType = sortType;
            this.accessToken = accessToken;
            this.nsfw = nsfw;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SubredditListingViewModel(retrofit, query, sortType, accessToken, nsfw);
        }
    }
}
