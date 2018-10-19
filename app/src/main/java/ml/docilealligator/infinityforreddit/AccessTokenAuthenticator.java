package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Authenticator;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import retrofit2.Call;
import retrofit2.Retrofit;

class AccessTokenAuthenticator implements Authenticator {
    private Retrofit mRetrofit;
    private SharedPreferences mAuthInfoSharedPreferences;

    AccessTokenAuthenticator(Retrofit retrofit, SharedPreferences authInfoSharedPreferences) {
        mRetrofit = retrofit;
        mAuthInfoSharedPreferences = authInfoSharedPreferences;
    }

    @Nullable
    @Override
    public Request authenticate(@NonNull Route route, @NonNull Response response) throws IOException {
        if (response.code() == 401) {
            String accessToken = response.request().header(RedditUtils.AUTHORIZATION_KEY).substring(RedditUtils.AUTHORIZATION_BASE.length());
            synchronized (this) {
                String accessTokenFromSharedPreferences = mAuthInfoSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
                if (accessToken.equals(accessTokenFromSharedPreferences)) {
                    String newAccessToken = refreshAccessToken();
                    if (!newAccessToken.equals("")) {
                        return response.request().newBuilder().headers(Headers.of(RedditUtils.getOAuthHeader(newAccessToken))).build();
                    } else {
                        return null;
                    }
                } else {
                    return response.request().newBuilder().headers(Headers.of(RedditUtils.getOAuthHeader(accessTokenFromSharedPreferences))).build();
                }
            }
        }
        return null;
    }

    private String refreshAccessToken() {
        String refreshToken = mAuthInfoSharedPreferences.getString(SharedPreferencesUtils.REFRESH_TOKEN_KEY, "");

        RedditAPI api = mRetrofit.create(RedditAPI.class);

        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.GRANT_TYPE_KEY, RedditUtils.GRANT_TYPE_REFRESH_TOKEN);
        params.put(RedditUtils.REFRESH_TOKEN_KEY, refreshToken);

        Call<String> accessTokenCall = api.getAccessToken(RedditUtils.getHttpBasicAuthHeader(), params);
        try {
            retrofit2.Response response = accessTokenCall.execute();
            JSONObject jsonObject = new JSONObject((String) response.body());

            String newAccessToken = jsonObject.getString(RedditUtils.ACCESS_TOKEN_KEY);

            mAuthInfoSharedPreferences.edit().putString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, newAccessToken).apply();

            Log.i("access token", newAccessToken);
            return newAccessToken;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "";
    }
}
