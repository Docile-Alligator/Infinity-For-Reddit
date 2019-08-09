package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.Locale;

import retrofit2.Retrofit;

public class PostViewModel extends ViewModel {
    private PostDataSourceFactory postDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<Boolean> hasPostLiveData;
    private LiveData<PagedList<Post>> posts;
    private MutableLiveData<String> sortTypeLiveData;

    public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, int postType, String sortType,
                         int filter) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, postType, sortType, filter);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getPaginationNetworkStateLiveData);
        hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::hasPostLiveData);

        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = Transformations.switchMap(sortTypeLiveData, sort -> {
            postDataSourceFactory.changeSortType(sortTypeLiveData.getValue());
            return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
        });
    }

    public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, String subredditName, int postType,
                         String sortType, int filter) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, subredditName,
                postType, sortType, filter);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getPaginationNetworkStateLiveData);
        hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::hasPostLiveData);

        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = Transformations.switchMap(sortTypeLiveData, sort -> {
            postDataSourceFactory.changeSortType(sortTypeLiveData.getValue());
            return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
        });
    }

    public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, String subredditName, int postType,
                         int filter) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, subredditName,
                postType, filter);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getPaginationNetworkStateLiveData);
        hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::hasPostLiveData);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
    }

    public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, String subredditName, String query,
                         int postType, String sortType, int filter) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, subredditName,
                query, postType, sortType, filter);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getPaginationNetworkStateLiveData);
        hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::hasPostLiveData);

        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = Transformations.switchMap(sortTypeLiveData, sort -> {
            postDataSourceFactory.changeSortType(sortTypeLiveData.getValue());
            return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
        });
    }

    LiveData<PagedList<Post>> getPosts() {
        return posts;
    }

    LiveData<NetworkState> getPaginationNetworkState() {
        return paginationNetworkState;
    }

    LiveData<NetworkState> getInitialLoadingState() {
        return initialLoadingState;
    }

    LiveData<Boolean> hasPost() {
        return hasPostLiveData;
    }

    void refresh() {
        postDataSourceFactory.getPostDataSource().invalidate();
    }

    void retry() {
        postDataSourceFactory.getPostDataSource().retry();
    }

    void retryLoadingMore() {
        postDataSourceFactory.getPostDataSource().retryLoadingMore();
    }

    void changeSortType(String sortType) {
        sortTypeLiveData.postValue(sortType);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Retrofit retrofit;
        private String accessToken;
        private Locale locale;
        private String subredditName;
        private String query;
        private int postType;
        private String sortType;
        private int filter;

        public Factory(Retrofit retrofit, String accessToken, Locale locale, int postType, String sortType,
                       int filter) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.locale = locale;
            this.postType = postType;
            this.sortType = sortType;
            this.filter = filter;
        }

        public Factory(Retrofit retrofit, String accessToken, Locale locale, String subredditName, int postType,
                       String sortType, int filter) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.locale = locale;
            this.subredditName = subredditName;
            this.postType = postType;
            this.sortType = sortType;
            this.filter = filter;
        }

        public Factory(Retrofit retrofit, String accessToken, Locale locale, String subredditName, int postType,
                       int filter) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.locale = locale;
            this.subredditName = subredditName;
            this.postType = postType;
            this.filter = filter;
        }

        public Factory(Retrofit retrofit, String accessToken, Locale locale, String subredditName, String query,
                       int postType, String sortType, int filter) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.locale = locale;
            this.subredditName = subredditName;
            this.query = query;
            this.postType = postType;
            this.sortType = sortType;
            this.filter = filter;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if(postType == PostDataSource.TYPE_FRONT_PAGE) {
                return (T) new PostViewModel(retrofit, accessToken, locale, postType, sortType, filter);
            } else if(postType == PostDataSource.TYPE_SEARCH){
                return (T) new PostViewModel(retrofit, accessToken, locale, subredditName, query, postType, sortType, filter);
            } else if(postType == PostDataSource.TYPE_SUBREDDIT) {
                return (T) new PostViewModel(retrofit, accessToken, locale, subredditName, postType, sortType, filter);
            } else {
                return (T) new PostViewModel(retrofit, accessToken, locale, subredditName, postType, filter);
            }
        }
    }
}
