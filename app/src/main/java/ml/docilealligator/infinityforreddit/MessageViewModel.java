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

public class MessageViewModel extends ViewModel {

  private final MessageDataSourceFactory messageDataSourceFactory;
  private final LiveData<NetworkState> paginationNetworkState;
  private final LiveData<NetworkState> initialLoadingState;
  private final LiveData<Boolean> hasMessageLiveData;
  private final LiveData<PagedList<Message>> messages;
  private final MutableLiveData<String> whereLiveData;

  public MessageViewModel(Retrofit retrofit, Locale locale, String accessToken, String where) {
    messageDataSourceFactory = new MessageDataSourceFactory(retrofit, locale, accessToken, where);

    initialLoadingState = Transformations
        .switchMap(messageDataSourceFactory.getMessageDataSourceLiveData(),
            MessageDataSource::getInitialLoadStateLiveData);
    paginationNetworkState = Transformations
        .switchMap(messageDataSourceFactory.getMessageDataSourceLiveData(),
            MessageDataSource::getPaginationNetworkStateLiveData);
    hasMessageLiveData = Transformations
        .switchMap(messageDataSourceFactory.getMessageDataSourceLiveData(),
            MessageDataSource::hasPostLiveData);

    whereLiveData = new MutableLiveData<>();
    whereLiveData.postValue(where);

    PagedList.Config pagedListConfig =
        (new PagedList.Config.Builder())
            .setEnablePlaceholders(false)
            .setPageSize(25)
            .build();

    messages = Transformations.switchMap(whereLiveData, newWhere -> {
      messageDataSourceFactory.changeWhere(whereLiveData.getValue());
      return (new LivePagedListBuilder(messageDataSourceFactory, pagedListConfig)).build();
    });
  }

  LiveData<PagedList<Message>> getMessages() {
    return messages;
  }

  LiveData<NetworkState> getPaginationNetworkState() {
    return paginationNetworkState;
  }

  LiveData<NetworkState> getInitialLoadingState() {
    return initialLoadingState;
  }

  LiveData<Boolean> hasMessage() {
    return hasMessageLiveData;
  }

  void refresh() {
    messageDataSourceFactory.getMessageDataSource().invalidate();
  }

  void retryLoadingMore() {
    messageDataSourceFactory.getMessageDataSource().retryLoadingMore();
  }

  void changeWhere(String where) {
    whereLiveData.postValue(where);
  }

  public static class Factory extends ViewModelProvider.NewInstanceFactory {

    private final Retrofit retrofit;
    private final Locale locale;
    private final String accessToken;
    private final String where;

    public Factory(Retrofit retrofit, Locale locale, String accessToken, String where) {
      this.retrofit = retrofit;
      this.locale = locale;
      this.accessToken = accessToken;
      this.where = where;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
      return (T) new MessageViewModel(retrofit, locale, accessToken, where);
    }
  }
}
