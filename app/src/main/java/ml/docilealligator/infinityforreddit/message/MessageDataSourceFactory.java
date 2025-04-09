package ml.docilealligator.infinityforreddit.message;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;

import java.util.Locale;
import java.util.concurrent.Executor;

import retrofit2.Retrofit;

class MessageDataSourceFactory extends DataSource.Factory {
    private final Executor executor;
    private final Handler handler;
    private final Retrofit oauthRetrofit;
    private final Locale locale;
    private final String accessToken;
    private String where;

    private MessageDataSource messageDataSource;
    private final MutableLiveData<MessageDataSource> messageDataSourceLiveData;

    MessageDataSourceFactory(Executor executor, Handler handler, Retrofit oauthRetrofit, Locale locale, String accessToken, String where) {
        this.executor = executor;
        this.handler = handler;
        this.oauthRetrofit = oauthRetrofit;
        this.locale = locale;
        this.accessToken = accessToken;
        this.where = where;
        messageDataSourceLiveData = new MutableLiveData<>();
    }

    @NonNull
    @Override
    public DataSource create() {
        messageDataSource = new MessageDataSource(executor, handler, oauthRetrofit, locale, accessToken, where);
        messageDataSourceLiveData.postValue(messageDataSource);
        return messageDataSource;
    }

    public MutableLiveData<MessageDataSource> getMessageDataSourceLiveData() {
        return messageDataSourceLiveData;
    }

    MessageDataSource getMessageDataSource() {
        return messageDataSource;
    }

    void changeWhere(String where) {
        this.where = where;
    }
}
