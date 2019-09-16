package ml.docilealligator.infinityforreddit;

import Account.Account;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import okhttp3.Authenticator;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Retrofit;

class AccessTokenAuthenticator implements Authenticator {

  private final Retrofit mRetrofit;
  private final RedditDataRoomDatabase mRedditDataRoomDatabase;

  AccessTokenAuthenticator(Retrofit retrofit, RedditDataRoomDatabase accountRoomDatabase) {
    mRetrofit = retrofit;
    mRedditDataRoomDatabase = accountRoomDatabase;
  }

  @Nullable
  @Override
  public Request authenticate(Route route, @NonNull Response response) {
    if (response.code() == 401) {
      String accessToken = response.request().header(RedditUtils.AUTHORIZATION_KEY)
          .substring(RedditUtils.AUTHORIZATION_BASE.length());
      synchronized (this) {
        Account account = mRedditDataRoomDatabase.accountDao().getCurrentAccount();
        if (account == null) {
          return null;
        }
        String accessTokenFromDatabase = account.getAccessToken();
        if (accessToken.equals(accessTokenFromDatabase)) {
          String newAccessToken = refreshAccessToken(account);
          if (!newAccessToken.equals("")) {
            return response.request().newBuilder()
                .headers(Headers.of(RedditUtils.getOAuthHeader(newAccessToken))).build();
          } else {
            return null;
          }
        } else {
          return response.request().newBuilder()
              .headers(Headers.of(RedditUtils.getOAuthHeader(accessTokenFromDatabase))).build();
        }
      }
    }
    return null;
  }

  private String refreshAccessToken(Account account) {
    String refreshToken = mRedditDataRoomDatabase.accountDao().getCurrentAccount()
        .getRefreshToken();

    RedditAPI api = mRetrofit.create(RedditAPI.class);

    Map<String, String> params = new HashMap<>();
    params.put(RedditUtils.GRANT_TYPE_KEY, RedditUtils.GRANT_TYPE_REFRESH_TOKEN);
    params.put(RedditUtils.REFRESH_TOKEN_KEY, refreshToken);

    Call<String> accessTokenCall = api.getAccessToken(RedditUtils.getHttpBasicAuthHeader(), params);
    try {
      retrofit2.Response response = accessTokenCall.execute();
      if (response.isSuccessful() && response.body() != null) {
        JSONObject jsonObject = new JSONObject((String) response.body());
        String newAccessToken = jsonObject.getString(RedditUtils.ACCESS_TOKEN_KEY);
        mRedditDataRoomDatabase.accountDao()
            .changeAccessToken(account.getUsername(), newAccessToken);

        return newAccessToken;
      }
      return "";
    } catch (IOException | JSONException e) {
      e.printStackTrace();
    }

    return "";
  }
}
