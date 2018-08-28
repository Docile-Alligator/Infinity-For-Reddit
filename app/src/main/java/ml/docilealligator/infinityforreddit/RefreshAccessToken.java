package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by alex on 3/13/18.
 */

class RefreshAccessToken {

    interface RefreshAccessTokenListener {
        void onRefreshAccessTokenSuccess();
        void onRefreshAccessTokenFail();
    }

    static void refreshAccessToken(final Context context, final RefreshAccessTokenListener refreshAccessTokenListener) {
        if(context != null) {
            String refreshToken = context.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.REFRESH_TOKEN_KEY, "");

            final Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(RedditUtils.API_BASE_URI)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            RedditAPI api = retrofit.create(RedditAPI.class);

            Map<String, String> params = new HashMap<>();
            params.put(RedditUtils.GRANT_TYPE_KEY, RedditUtils.GRANT_TYPE_REFRESH_TOKEN);
            params.put(RedditUtils.REFRESH_TOKEN_KEY, refreshToken);

            Call<String> accessTokenCall = api.getAccessToken(RedditUtils.getHttpBasicAuthHeader(), params);
            accessTokenCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body());
                        String newAccessToken = jsonObject.getString(RedditUtils.ACCESS_TOKEN_KEY);
                        int expireIn = jsonObject.getInt(RedditUtils.EXPIRES_IN_KEY);

                        SharedPreferences.Editor editor = context.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).edit();
                        editor.putString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, newAccessToken);
                        editor.putInt(SharedPreferencesUtils.ACCESS_TOKEN_EXPIRE_INTERVAL_KEY, expireIn);
                        editor.apply();

                        long queryAccessTokenTime = Calendar.getInstance().getTimeInMillis();
                        editor.putLong(SharedPreferencesUtils.QUERY_ACCESS_TOKEN_TIME_KEY, queryAccessTokenTime);
                        editor.apply();

                        Log.i("access token", newAccessToken);
                        refreshAccessTokenListener.onRefreshAccessTokenSuccess();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        refreshAccessTokenListener.onRefreshAccessTokenFail();
                        Log.i("main activity", "Error parsing JSON object when getting the access token");
                    }
                    refreshAccessTokenListener.onRefreshAccessTokenSuccess();
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Log.i("call failed", t.getMessage());
                    refreshAccessTokenListener.onRefreshAccessTokenFail();
                }
            });
        }
    }
}
