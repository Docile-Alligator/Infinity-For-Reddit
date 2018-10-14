package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

class AccessTokenAuthenticator implements Authenticator {
    private SharedPreferences mAuthInfoSharedPreferences;

    AccessTokenAuthenticator(SharedPreferences authInfoSharedPreferences) {
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
                    String newAccessToken = RefreshAccessToken.refreshAccessToken(mAuthInfoSharedPreferences);
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
}
