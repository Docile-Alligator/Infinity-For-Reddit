package ml.docilealligator.infinityforreddit.comment;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.Locale;

import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.SortType;
import retrofit2.Retrofit;

public class CommentViewModel extends ViewModel {
    private CommentDataSourceFactory commentDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<Boolean> hasCommentLiveData;
    private LiveData<PagedList<Comment>> comments;
    private MutableLiveData<SortType> sortTypeLiveData;

    public CommentViewModel(Retrofit retrofit, Locale locale, String accessToken, String username, SortType sortType,
                            boolean areSavedComments) {
        commentDataSourceFactory = new CommentDataSourceFactory(retrofit, locale, accessToken, username, sortType,
                areSavedComments);

        initialLoadingState = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                CommentDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                CommentDataSource::getPaginationNetworkStateLiveData);
        hasCommentLiveData = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                CommentDataSource::hasPostLiveData);

        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
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
        private Retrofit retrofit;
        private Locale locale;
        private String accessToken;
        private String username;
        private SortType sortType;
        private boolean areSavedComments;

        public Factory(Retrofit retrofit, Locale locale, String accessToken, String username,
                       SortType sortType, boolean areSavedComments) {
            this.retrofit = retrofit;
            this.locale = locale;
            this.accessToken = accessToken;
            this.username = username;
            this.sortType = sortType;
            this.areSavedComments = areSavedComments;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CommentViewModel(retrofit, locale, accessToken, username, sortType, areSavedComments);
        }
    }
}
