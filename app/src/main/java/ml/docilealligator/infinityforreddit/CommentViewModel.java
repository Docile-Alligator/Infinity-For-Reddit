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

public class CommentViewModel extends ViewModel {

  private final CommentDataSourceFactory commentDataSourceFactory;
  private final LiveData<NetworkState> paginationNetworkState;
  private final LiveData<NetworkState> initialLoadingState;
  private final LiveData<Boolean> hasCommentLiveData;
  private final LiveData<PagedList<CommentData>> comments;
  private final MutableLiveData<String> sortTypeLiveData;

  public CommentViewModel(Retrofit retrofit, Locale locale, String username, String sortType) {
    commentDataSourceFactory = new CommentDataSourceFactory(retrofit, locale, username, sortType);

    initialLoadingState = Transformations
        .switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
            CommentDataSource::getInitialLoadStateLiveData);
    paginationNetworkState = Transformations
        .switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
            CommentDataSource::getPaginationNetworkStateLiveData);
    hasCommentLiveData = Transformations
        .switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
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

  void retryLoadingMore() {
    commentDataSourceFactory.getCommentDataSource().retryLoadingMore();
  }

  void changeSortType(String sortType) {
    sortTypeLiveData.postValue(sortType);
  }

  public static class Factory extends ViewModelProvider.NewInstanceFactory {

    private final Retrofit retrofit;
    private final Locale locale;
    private final String username;
    private final String sortType;

    public Factory(Retrofit retrofit, Locale locale, String username, String sortType) {
      this.retrofit = retrofit;
      this.locale = locale;
      this.username = username;
      this.sortType = sortType;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T) new CommentViewModel(retrofit, locale, username, sortType);
    }
  }
}
