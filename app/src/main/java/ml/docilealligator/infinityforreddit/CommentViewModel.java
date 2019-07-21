package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.Locale;

import retrofit2.Retrofit;

public class CommentViewModel extends ViewModel {
    private CommentDataSourceFactory commentDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<PagedList<CommentData>> comments;

    public CommentViewModel(Retrofit retrofit, Locale locale, String username,
                         CommentDataSource.OnCommentFetchedCallback onCommentFetchedCallback) {
        commentDataSourceFactory = new CommentDataSourceFactory(retrofit, locale, username, onCommentFetchedCallback);

        initialLoadingState = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                (Function<CommentDataSource, LiveData<NetworkState>>) CommentDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                (Function<CommentDataSource, LiveData<NetworkState>>) CommentDataSource::getPaginationNetworkStateLiveData);
        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        comments = (new LivePagedListBuilder(commentDataSourceFactory, pagedListConfig)).build();
    }

    LiveData<PagedList<CommentData>> getComments() {
        return comments;
    }

    LiveData<NetworkState> getPaginationNetworkState() {
        return paginationNetworkState;
    }

    LiveData<NetworkState> getInitialLoadingState() {
        return initialLoadingState;
    }

    void refresh() {
        commentDataSourceFactory.getCommentDataSource().invalidate();
    }

    void retry() {
        commentDataSourceFactory.getCommentDataSource().retry();
    }

    void retryLoadingMore() {
        commentDataSourceFactory.getCommentDataSource().retryLoadingMore();
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Retrofit retrofit;
        private Locale locale;
        private String username;
        private CommentDataSource.OnCommentFetchedCallback onCommentFetchedCallback;

        public Factory(Retrofit retrofit, Locale locale, String username,
                       CommentDataSource.OnCommentFetchedCallback onCommentFetchedCallback) {
            this.retrofit = retrofit;
            this.locale = locale;
            this.username = username;
            this.onCommentFetchedCallback = onCommentFetchedCallback;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CommentViewModel(retrofit, locale, username, onCommentFetchedCallback);
        }
    }
}
