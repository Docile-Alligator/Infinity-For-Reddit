package ml.docilealligator.infinityforreddit.network;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.apis.ServerAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import okhttp3.Authenticator;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ServerAccessTokenAuthenticator implements Authenticator {
    private final RedditDataRoomDatabase mRedditDataRoomDatabase;
    private final SharedPreferences mCurrentAccountSharedPreferences;

    public ServerAccessTokenAuthenticator(RedditDataRoomDatabase redditDataRoomDatabase,
                                          SharedPreferences currentAccountSharedPreferences) {
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mCurrentAccountSharedPreferences = currentAccountSharedPreferences;
    }

    @Nullable
    @Override
    public Request authenticate(@Nullable Route route, @NonNull Response response) throws IOException {
        if (response.code() == 401) {
            String accessTokenHeader = response.request().header(APIUtils.AUTHORIZATION_KEY);
            if (accessTokenHeader == null) {
                return null;
            }

            String accessToken = accessTokenHeader.substring(APIUtils.AUTHORIZATION_BASE.length());
            synchronized (this) {
                Account account = mRedditDataRoomDatabase.accountDao().getCurrentAccount();
                if (account == null) {
                    return null;
                }
                // TODO server access token
                String accessTokenFromDatabase = account.getAccessToken();
                if (accessToken.equals(accessTokenFromDatabase)) {
                    String newAccessToken = refreshAccessToken(account);
                    if (!newAccessToken.isEmpty()) {
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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIUtils.SERVER_API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        // TODO server refresh token
        String refreshToken = mRedditDataRoomDatabase.accountDao().getCurrentAccount().getRefreshToken();

        Call<String> accessTokenCall = retrofit.create(ServerAPI.class).refreshAccessToken(account.getAccountName(), refreshToken);
        try {
            retrofit2.Response<String> response = accessTokenCall.execute();
            if (response.isSuccessful() && response.body() != null) {
                String newAccessToken = new JSONObject(response.body()).getString(APIUtils.ACCESS_TOKEN_KEY);
                mRedditDataRoomDatabase.accountDao().updateAccessToken(account.getAccountName(), newAccessToken);
                if (mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, Account.ANONYMOUS_ACCOUNT).equals(account.getAccountName())) {
                    // TODO server access token
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
