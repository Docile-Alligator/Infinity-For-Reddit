package ml.docilealligator.infinityforreddit.message;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.NetworkState;
import retrofit2.Retrofit;

class MessageDataSource extends PageKeyedDataSource<String, Message> {
    private final Executor executor;
    private final Handler handler;
    private final Retrofit oauthRetrofit;
    private final Locale locale;
    private final String accessToken;
    private final String where;
    private final int messageType;

    private final MutableLiveData<NetworkState> paginationNetworkStateLiveData;
    private final MutableLiveData<NetworkState> initialLoadStateLiveData;
    private final MutableLiveData<Boolean> hasPostLiveData;

    private LoadParams<String> params;
    private LoadCallback<String, Message> callback;

    MessageDataSource(Executor executor, Handler handler, Retrofit oauthRetrofit, Locale locale, String accessToken, String where) {
        this.executor = executor;
        this.handler = handler;
        this.oauthRetrofit = oauthRetrofit;
        this.locale = locale;
        this.accessToken = accessToken;
        this.where = where;
        if (where.equals(FetchMessage.WHERE_MESSAGES)) {
            messageType = FetchMessage.MESSAGE_TYPE_PRIVATE_MESSAGE;
        } else {
            messageType = FetchMessage.MESSAGE_TYPE_INBOX;
        }
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
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull LoadInitialCallback<String, Message> callback) {
        initialLoadStateLiveData.postValue(NetworkState.LOADING);

        FetchMessage.fetchInbox(executor, handler, oauthRetrofit, locale, accessToken, where, null, messageType,
                new FetchMessage.FetchMessagesListener() {
            @Override
            public void fetchSuccess(List<Message> messages, @Nullable String after) {
                if (messages.isEmpty()) {
                    hasPostLiveData.postValue(false);
                } else {
                    hasPostLiveData.postValue(true);
                }

                if (after == null || after.isEmpty() || after.equals("null")) {
                    callback.onResult(messages, null, null);
                } else {
                    callback.onResult(messages, null, after);
                }
                initialLoadStateLiveData.postValue(NetworkState.LOADED);
            }

            @Override
            public void fetchFailed() {
                initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error fetch messages"));
            }
        });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Message> callback) {

    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Message> callback) {
        this.params = params;
        this.callback = callback;

        paginationNetworkStateLiveData.postValue(NetworkState.LOADING);

        FetchMessage.fetchInbox(executor, handler, oauthRetrofit, locale, accessToken, where, params.key, messageType,
                new FetchMessage.FetchMessagesListener() {
            @Override
            public void fetchSuccess(List<Message> messages, @Nullable String after) {
                if (after == null || after.isEmpty() || after.equals("null")) {
                    callback.onResult(messages, null);
                } else {
                    callback.onResult(messages, after);
                }

                paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
            }

            @Override
            public void fetchFailed() {
                paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error fetching data"));
            }
        });
    }
}
