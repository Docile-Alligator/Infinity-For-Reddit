package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.apis.RedgifsAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RedgifsAccessTokenAuthenticator implements Interceptor {
    private SharedPreferences mCurrentAccountSharedPreferences;

    public RedgifsAccessTokenAuthenticator(SharedPreferences currentAccountSharedPreferences) {
        this.mCurrentAccountSharedPreferences = currentAccountSharedPreferences;
    }

    private String refreshAccessToken() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(APIUtils.REDGIFS_API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        RedgifsAPI api = retrofit.create(RedgifsAPI.class);

        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.GRANT_TYPE_KEY, APIUtils.GRANT_TYPE_CLIENT_CREDENTIALS);
        params.put(APIUtils.CLIENT_ID_KEY, APIUtils.REDGIFS_CLIENT_ID);
        params.put(APIUtils.CLIENT_SECRET_KEY, APIUtils.REDGIFS_CLIENT_SECRET);

        Call<String> accessTokenCall = api.getRedgifsAccessToken(params);
        try {
            retrofit2.Response<String> response = accessTokenCall.execute();
            if (response.isSuccessful() && response.body() != null) {
                String newAccessToken = new JSONObject(response.body()).getString(APIUtils.ACCESS_TOKEN_KEY);
                mCurrentAccountSharedPreferences.edit().putString(SharedPreferencesUtils.REDGIFS_ACCESS_TOKEN, newAccessToken).apply();

                return newAccessToken;
            }
            return "";
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if (response.code() == 401 || response.code() == 400) {
            String accessTokenHeader = response.request().header(APIUtils.AUTHORIZATION_KEY);
            if (accessTokenHeader == null) {
                return response;
            }

            String accessToken = accessTokenHeader.substring(APIUtils.AUTHORIZATION_BASE.length() - 1).trim();
            synchronized (this) {
                String accessTokenFromSharedPreferences = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.REDGIFS_ACCESS_TOKEN, "");
                if (accessToken.equals(accessTokenFromSharedPreferences)) {
                    String newAccessToken = refreshAccessToken();
                    if (!newAccessToken.equals("")) {
                        response.close();
                        return chain.proceed(response.request().newBuilder().headers(Headers.of(APIUtils.getRedgifsOAuthHeader(newAccessToken))).build());
                    } else {
                        return response;
                    }
                } else {
                    response.close();
                    return chain.proceed(response.request().newBuilder().headers(Headers.of(APIUtils.getRedgifsOAuthHeader(accessTokenFromSharedPreferences))).build());
                }
            }
        }
        return response;
    }
}
