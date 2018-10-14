package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by alex on 3/13/18.
 */

class RefreshAccessToken {
    static String refreshAccessToken(/*Retrofit oauthRetrofit, */SharedPreferences sharedPreferences) {
        String refreshToken = sharedPreferences.getString(SharedPreferencesUtils.REFRESH_TOKEN_KEY, "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RedditUtils.API_BASE_URI)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        RedditAPI api = retrofit.create(RedditAPI.class);

        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.GRANT_TYPE_KEY, RedditUtils.GRANT_TYPE_REFRESH_TOKEN);
        params.put(RedditUtils.REFRESH_TOKEN_KEY, refreshToken);

        Call<String> accessTokenCall = api.getAccessToken(RedditUtils.getHttpBasicAuthHeader(), params);
        try {
            Response response = accessTokenCall.execute();
            JSONObject jsonObject = new JSONObject((String) response.body());

            String newAccessToken = jsonObject.getString(RedditUtils.ACCESS_TOKEN_KEY);

            sharedPreferences.edit().putString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, newAccessToken).apply();

            Log.i("access token", newAccessToken);
            return newAccessToken;
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "";
    }
}
