package ml.docilealligator.infinityforreddit;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import java.util.Locale;

import retrofit2.Retrofit;

public class PostViewModel extends ViewModel {
    private PostDataSourceFactory postDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<PagedList<Post>> posts;

    public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, boolean isBestPost) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, isBestPost);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                dataSource -> dataSource.getInitialLoadStateLiveData());
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                dataSource -> dataSource.getPaginationNetworkStateLiveData());
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
    }

    public PostViewModel(Retrofit retrofit, Locale locale, boolean isBestPost, String subredditName) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, locale, isBestPost, subredditName);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                dataSource -> dataSource.getInitialLoadStateLiveData());
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                dataSource -> dataSource.getPaginationNetworkStateLiveData());

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
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

    void refresh() {
        postDataSourceFactory.getPostDataSource().invalidate();
    }

    void retry() {
        postDataSourceFactory.getPostDataSource().retry();
    }

    void retryLoadingMore() {
        postDataSourceFactory.getPostDataSource().retryLoadingMore();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Retrofit retrofit;
        private String accessToken;
        private Locale locale;
        private boolean isBestPost;
        private String subredditName;

        public Factory(Retrofit retrofit, String accessToken, Locale locale, boolean isBestPost) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.locale = locale;
            this.isBestPost = isBestPost;
        }

        public Factory(Retrofit retrofit, Locale locale, boolean isBestPost, String subredditName) {
            this.retrofit = retrofit;
            this.locale = locale;
            this.isBestPost = isBestPost;
            this.subredditName = subredditName;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if(isBestPost) {
                return (T) new PostViewModel(retrofit, accessToken, locale, isBestPost);
            } else {
                return (T) new PostViewModel(retrofit, locale, isBestPost, subredditName);
            }
        }
    }
}
