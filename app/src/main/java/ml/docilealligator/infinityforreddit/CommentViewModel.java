package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
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
    private LiveData<Boolean> hasCommentLiveData;
    private LiveData<PagedList<CommentData>> comments;

    public CommentViewModel(Retrofit retrofit, Locale locale, String username) {
        commentDataSourceFactory = new CommentDataSourceFactory(retrofit, locale, username);

        initialLoadingState = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                CommentDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                CommentDataSource::getPaginationNetworkStateLiveData);
        hasCommentLiveData = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                CommentDataSource::hasPostLiveData);
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

    LiveData<Boolean> hasComment() {
        return hasCommentLiveData;
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

        public Factory(Retrofit retrofit, Locale locale, String username) {
            this.retrofit = retrofit;
            this.locale = locale;
            this.username = username;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CommentViewModel(retrofit, locale, username);
        }
    }
}
