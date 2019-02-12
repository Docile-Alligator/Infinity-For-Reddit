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

public class CommentViewModel extends ViewModel {
    private CommentDataSourceFactory commentDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<PagedList<CommentData>> comments;

    public CommentViewModel(Retrofit retrofit, Locale locale, String subredditNamePrefixed,
                            String article, String comment, boolean isPost,
                            CommentDataSource.OnCommentFetchedCallback onCommentFetchedCallback) {
        commentDataSourceFactory = new CommentDataSourceFactory(retrofit, locale, subredditNamePrefixed, article, comment, isPost, onCommentFetchedCallback);

        initialLoadingState = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceMutableLiveData(),
                dataSource -> dataSource.getInitialLoadStateLiveData());
        paginationNetworkState = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceMutableLiveData(),
                dataSource -> dataSource.getPaginationNetworkStateLiveData());
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
        private String subredditNamePrefixed;
        private String article;
        private String comment;
        private boolean isPost;
        private CommentDataSource.OnCommentFetchedCallback onCommentFetchedCallback;

        public Factory(Retrofit retrofit, Locale locale, String subredditNamePrefixed,
                       String article, String comment, boolean isPost,
                       CommentDataSource.OnCommentFetchedCallback onCommentFetchedCallback) {
            this.retrofit = retrofit;
            this.locale = locale;
            this.subredditNamePrefixed = subredditNamePrefixed;
            this.article = article;
            this.comment = comment;
            this.isPost = isPost;
            this.onCommentFetchedCallback = onCommentFetchedCallback;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CommentViewModel(retrofit, locale, subredditNamePrefixed, article, comment, isPost, onCommentFetchedCallback);
        }
    }
}
