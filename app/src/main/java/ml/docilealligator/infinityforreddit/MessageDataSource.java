package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;
import java.util.ArrayList;
import java.util.Locale;
import retrofit2.Retrofit;

class MessageDataSource extends PageKeyedDataSource<String, Message> {

  private final Retrofit oauthRetrofit;
  private final Locale locale;
  private final String accessToken;
  private final String where;

  private final MutableLiveData<NetworkState> paginationNetworkStateLiveData;
  private final MutableLiveData<NetworkState> initialLoadStateLiveData;
  private final MutableLiveData<Boolean> hasPostLiveData;

  private LoadParams<String> params;
  private LoadCallback<String, Message> callback;

  MessageDataSource(Retrofit oauthRetrofit, Locale locale, String accessToken, String where) {
    this.oauthRetrofit = oauthRetrofit;
    this.locale = locale;
    this.accessToken = accessToken;
    this.where = where;
    paginationNetworkStateLiveData = new MutableLiveData<>();
    initialLoadStateLiveData = new MutableLiveData<>();
    hasPostLiveData = new MutableLiveData<>();
  }

  MutableLiveData<NetworkState> getPaginationNetworkStateLiveData() {
    return paginationNetworkStateLiveData;
  }

  MutableLiveData<NetworkState> getInitialLoadStateLiveData() {
    return initialLoadStateLiveData;
  }

  MutableLiveData<Boolean> hasPostLiveData() {
    return hasPostLiveData;
  }

  void retryLoadingMore() {
    loadAfter(params, callback);
  }

  @Override
  public void loadInitial(@NonNull LoadInitialParams<String> params,
      @NonNull LoadInitialCallback<String, Message> callback) {
    initialLoadStateLiveData.postValue(NetworkState.LOADING);

    FetchMessages.fetchMessagesAsync(oauthRetrofit, locale, accessToken, where, null,
        new FetchMessages.FetchMessagesListener() {
          @Override
          public void fetchSuccess(ArrayList<Message> messages, @Nullable String after) {
            if (messages.size() == 0) {
              hasPostLiveData.postValue(false);
            } else {
              hasPostLiveData.postValue(true);
            }

            if (after == null || after.equals("") || after.equals("null")) {
              callback.onResult(messages, null, null);
            } else {
              callback.onResult(messages, null, after);
            }
            initialLoadStateLiveData.postValue(NetworkState.LOADED);
          }

          @Override
          public void fetchFailed() {
            initialLoadStateLiveData
                .postValue(new NetworkState(NetworkState.Status.FAILED, "Error fetch messages"));
          }
        });
  }

  @Override
  public void loadBefore(@NonNull LoadParams<String> params,
      @NonNull LoadCallback<String, Message> callback) {

  }

  @Override
  public void loadAfter(@NonNull LoadParams<String> params,
      @NonNull LoadCallback<String, Message> callback) {
    this.params = params;
    this.callback = callback;

    paginationNetworkStateLiveData.postValue(NetworkState.LOADING);

    FetchMessages.fetchMessagesAsync(oauthRetrofit, locale, accessToken, where, params.key,
        new FetchMessages.FetchMessagesListener() {
          @Override
          public void fetchSuccess(ArrayList<Message> messages, @Nullable String after) {
            if (after == null || after.equals("") || after.equals("null")) {
              callback.onResult(messages, null);
            } else {
              callback.onResult(messages, after);
            }

            paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
          }

          @Override
          public void fetchFailed() {
            paginationNetworkStateLiveData
                .postValue(new NetworkState(NetworkState.Status.FAILED, "Error fetching data"));
          }
        });
  }
}
