package ml.docilealligator.infinityforreddit.message;

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
import retrofit2.Retrofit;

public class MessageViewModel extends ViewModel {
    private MessageDataSourceFactory messageDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<Boolean> hasMessageLiveData;
    private LiveData<PagedList<Message>> messages;
    private MutableLiveData<String> whereLiveData;

    public MessageViewModel(Retrofit retrofit, Locale locale, String accessToken, String where) {
        messageDataSourceFactory = new MessageDataSourceFactory(retrofit, locale, accessToken, where);

        initialLoadingState = Transformations.switchMap(messageDataSourceFactory.getMessageDataSourceLiveData(),
                MessageDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(messageDataSourceFactory.getMessageDataSourceLiveData(),
                MessageDataSource::getPaginationNetworkStateLiveData);
        hasMessageLiveData = Transformations.switchMap(messageDataSourceFactory.getMessageDataSourceLiveData(),
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

    public LiveData<PagedList<Message>> getMessages() {
        return messages;
    }

    public LiveData<NetworkState> getPaginationNetworkState() {
        return paginationNetworkState;
    }

    public LiveData<NetworkState> getInitialLoadingState() {
        return initialLoadingState;
    }

    public LiveData<Boolean> hasMessage() {
        return hasMessageLiveData;
    }

    public void refresh() {
        messageDataSourceFactory.getMessageDataSource().invalidate();
    }

    public void retryLoadingMore() {
        messageDataSourceFactory.getMessageDataSource().retryLoadingMore();
    }

    void changeWhere(String where) {
        whereLiveData.postValue(where);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Retrofit retrofit;
        private Locale locale;
        private String accessToken;
        private String where;

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
