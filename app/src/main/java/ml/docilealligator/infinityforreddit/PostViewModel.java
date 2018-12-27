package ml.docilealligator.infinityforreddit;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.concurrent.Executor;

import retrofit2.Retrofit;

public class PostViewModel extends ViewModel {
    private Executor executor;
    private LiveData<NetworkState> networkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<PagedList<Post>> posts;

    public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, boolean isBestPost) {
        //executor = Executors.newFixedThreadPool(5);

        PostDataSourceFactory postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, isBestPost);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getMutableLiveData(),
                dataSource -> dataSource.getInitialLoading());
        networkState = Transformations.switchMap(postDataSourceFactory.getMutableLiveData(),
                dataSource -> dataSource.getNetworkState());
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
    }

    public PostViewModel(Retrofit retrofit, Locale locale, boolean isBestPost, String subredditName) {
        //executor = Executors.newFixedThreadPool(5);

        PostDataSourceFactory postDataSourceFactory = new PostDataSourceFactory(retrofit, locale, isBestPost, subredditName);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getMutableLiveData(),
                dataSource -> dataSource.getInitialLoading());
        networkState = Transformations.switchMap(postDataSourceFactory.getMutableLiveData(),
                dataSource -> dataSource.getNetworkState());

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

    LiveData<NetworkState> getNetworkState() {
        return networkState;
    }

    public LiveData<NetworkState> getInitialLoadingState() {
        return initialLoadingState;
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
