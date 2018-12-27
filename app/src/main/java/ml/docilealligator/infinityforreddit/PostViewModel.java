package ml.docilealligator.infinityforreddit;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PageKeyedDataSource;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import java.util.Locale;
import java.util.concurrent.Executor;

import retrofit2.Retrofit;

public class PostViewModel extends ViewModel {
    private Executor executor;
    LiveData<NetworkState> networkState;
    LiveData<PagedList<Post>> posts;
    LiveData<PageKeyedDataSource<String, Post>> liveDataSource;

    public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, boolean isBestPost) {
        //executor = Executors.newFixedThreadPool(5);

        PostDataSourceFactory postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, isBestPost);
        /*networkState = Transformations.switchMap(postDataSourceFactory.getMutableLiveData(),
                (Function<PostDataSource, LiveData<NetworkState>>) PostDataSource::getNetworkState);*/
        liveDataSource = postDataSourceFactory.getMutableLiveData();

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

    /*public LiveData<NetworkState> getNetworkState() {
        return networkState;
    }*/

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Retrofit retrofit;
        private String accessToken;
        private Locale locale;
        private boolean isBestPost;

        public Factory(Retrofit retrofit, String accessToken, Locale locale, boolean isBestPost) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.locale = locale;
            this.isBestPost = isBestPost;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new PostViewModel(retrofit, accessToken, locale, isBestPost);
        }
    }
}
