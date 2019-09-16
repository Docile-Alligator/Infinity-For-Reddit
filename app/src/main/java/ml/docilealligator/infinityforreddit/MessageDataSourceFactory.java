package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import java.util.Locale;
import retrofit2.Retrofit;

class MessageDataSourceFactory extends DataSource.Factory {

  private final Retrofit oauthRetrofit;
  private final Locale locale;
  private final String accessToken;
  private final MutableLiveData<MessageDataSource> messageDataSourceLiveData;
  private String where;
  private MessageDataSource messageDataSource;

  MessageDataSourceFactory(Retrofit oauthRetrofit, Locale locale, String accessToken,
      String where) {
    this.oauthRetrofit = oauthRetrofit;
    this.locale = locale;
    this.accessToken = accessToken;
    this.where = where;
    messageDataSourceLiveData = new MutableLiveData<>();
  }

  @NonNull
  @Override
  public DataSource create() {
    messageDataSource = new MessageDataSource(oauthRetrofit, locale, accessToken, where);
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
