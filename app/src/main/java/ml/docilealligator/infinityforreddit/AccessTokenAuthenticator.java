package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import okhttp3.Authenticator;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Retrofit;

class AccessTokenAuthenticator implements Authenticator {
    private Retrofit mRetrofit;
    private RedditDataRoomDatabase mRedditDataRoomDatabase;
    private SharedPreferences mCurrentAccountSharedPreferences;

    AccessTokenAuthenticator(Retrofit retrofit, RedditDataRoomDatabase accountRoomDatabase, SharedPreferences currentAccountSharedPreferences) {
        mRetrofit = retrofit;
        mRedditDataRoomDatabase = accountRoomDatabase;
        mCurrentAccountSharedPreferences = currentAccountSharedPreferences;
    }

    @Nullable
    @Override
    public Request authenticate(Route route, @NonNull Response response) {
        if (response.code() == 401) {
            String accessToken = response.request().header(APIUtils.AUTHORIZATION_KEY).substring(APIUtils.AUTHORIZATION_BASE.length());
            synchronized (this) {
                Account account = mRedditDataRoomDatabase.accountDao().getCurrentAccount();
                if (account == null) {
                    return null;
                }
                String accessTokenFromDatabase = account.getAccessToken();
                if (accessToken.equals(accessTokenFromDatabase)) {
                    String newAccessToken = refreshAccessToken(account);
                    if (!newAccessToken.equals("")) {
                        return response.request().newBuilder().headers(Headers.of(APIUtils.getOAuthHeader(newAccessToken))).build();
                    } else {
                        return null;
                    }
                } else {
                    return response.request().newBuilder().headers(Headers.of(APIUtils.getOAuthHeader(accessTokenFromDatabase))).build();
                }
            }
        }
        return null;
    }

    private String refreshAccessToken(Account account) {
        String refreshToken = mRedditDataRoomDatabase.accountDao().getCurrentAccount().getRefreshToken();

        RedditAPI api = mRetrofit.create(RedditAPI.class);

        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.GRANT_TYPE_KEY, APIUtils.GRANT_TYPE_REFRESH_TOKEN);
        params.put(APIUtils.REFRESH_TOKEN_KEY, refreshToken);

        Call<String> accessTokenCall = api.getAccessToken(APIUtils.getHttpBasicAuthHeader(), params);
        try {
            retrofit2.Response<String> response = accessTokenCall.execute();
            if (response.isSuccessful() && response.body() != null) {
                JSONObject jsonObject = new JSONObject(response.body());
                String newAccessToken = jsonObject.getString(APIUtils.ACCESS_TOKEN_KEY);
                String newRefreshToken = jsonObject.has(APIUtils.REFRESH_TOKEN_KEY) ? jsonObject.getString(APIUtils.REFRESH_TOKEN_KEY) : null;
                if (newRefreshToken == null) {
                    mRedditDataRoomDatabase.accountDao().updateAccessToken(account.getAccountName(), newAccessToken);
                } else {
                    mRedditDataRoomDatabase.accountDao().updateAccessTokenAndRefreshToken(account.getAccountName(), newAccessToken, newRefreshToken);
                }
                if (mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, "").equals(account.getAccountName())) {
                    mCurrentAccountSharedPreferences.edit().putString(SharedPreferencesUtils.ACCESS_TOKEN, newAccessToken).apply();
                }

                return newAccessToken;
            }
            return "";
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "";
    }
}
