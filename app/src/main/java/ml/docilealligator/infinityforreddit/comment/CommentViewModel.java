package ml.docilealligator.infinityforreddit.comment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class CommentViewModel extends ViewModel {
    private final CommentDataSourceFactory commentDataSourceFactory;
    private final LiveData<NetworkState> paginationNetworkState;
    private final LiveData<NetworkState> initialLoadingState;
    private final LiveData<Boolean> hasCommentLiveData;
    private final LiveData<PagedList<Comment>> comments;
    private final MutableLiveData<SortType> sortTypeLiveData;

    public CommentViewModel(Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName,
                            String username, SortType sortType, boolean areSavedComments) {
        commentDataSourceFactory = new CommentDataSourceFactory(retrofit, accessToken, accountName,
                username, sortType, areSavedComments);

        initialLoadingState = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                CommentDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                CommentDataSource::getPaginationNetworkStateLiveData);
        hasCommentLiveData = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                CommentDataSource::hasPostLiveData);

        sortTypeLiveData = new MutableLiveData<>(sortType);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(100)
                        .setPrefetchDistance(10)
                        .setInitialLoadSizeHint(10)
                        .build();

        comments = Transformations.switchMap(sortTypeLiveData, sort -> {
            commentDataSourceFactory.changeSortType(sortTypeLiveData.getValue());
            return (new LivePagedListBuilder(commentDataSourceFactory, pagedListConfig)).build();
        });
    }

    public LiveData<PagedList<Comment>> getComments() {
        return comments;
    }

    public LiveData<NetworkState> getPaginationNetworkState() {
        return paginationNetworkState;
    }

    public LiveData<NetworkState> getInitialLoadingState() {
        return initialLoadingState;
    }

    public LiveData<Boolean> hasComment() {
        return hasCommentLiveData;
    }

    public void refresh() {
        commentDataSourceFactory.getCommentDataSource().invalidate();
    }

    public void retryLoadingMore() {
        commentDataSourceFactory.getCommentDataSource().retryLoadingMore();
    }

    public void changeSortType(SortType sortType) {
        sortTypeLiveData.postValue(sortType);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final Retrofit retrofit;
        private final String accessToken;
        private final String accountName;
        private final String username;
        private final SortType sortType;
        private final boolean areSavedComments;

        public Factory(Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName, String username,
                       SortType sortType, boolean areSavedComments) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.username = username;
            this.sortType = sortType;
            this.areSavedComments = areSavedComments;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CommentViewModel(retrofit, accessToken, accountName, username,
                    sortType, areSavedComments);
        }
    }
}
