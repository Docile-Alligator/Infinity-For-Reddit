package ml.docilealligator.infinityforreddit.user;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SelectUserFlair {
    public interface SelectUserFlairListener {
        void success();
        void failed(String errorMessage);
    }

    public static void selectUserFlair(Executor executor, Handler handler, Retrofit oauthRetrofit, String accessToken, @Nullable UserFlair userFlair,
                                       String subredditName, @NonNull String accountName, SelectUserFlairListener selectUserFlairListener) {

        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.API_TYPE_KEY, APIUtils.API_TYPE_JSON);
        if (userFlair != null) {
            params.put(APIUtils.FLAIR_TEMPLATE_ID_KEY, userFlair.getId());
            params.put(APIUtils.TEXT_KEY, userFlair.getText());
        }
        params.put(APIUtils.NAME_KEY, accountName);

        oauthRetrofit.create(RedditAPI.class).selectUserFlair(APIUtils.getOAuthHeader(accessToken), params, subredditName).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> {
                        try {
                            JSONObject responseObject = new JSONObject(response.body()).getJSONObject(JSONUtils.JSON_KEY);

                            if (responseObject.getJSONArray(JSONUtils.ERRORS_KEY).length() != 0) {
                                JSONArray error = responseObject.getJSONArray(JSONUtils.ERRORS_KEY)
                                        .getJSONArray(responseObject.getJSONArray(JSONUtils.ERRORS_KEY).length() - 1);
                                if (error.length() != 0) {
                                    String errorString;
                                    if (error.length() >= 2) {
                                        errorString = error.getString(1);
                                    } else {
                                        errorString = error.getString(0);
                                    }

                                    handler.post(() -> selectUserFlairListener.failed(errorString.substring(0, 1).toUpperCase() + errorString.substring(1)));
                                } else {
                                    handler.post(selectUserFlairListener::success);
                                }
                            } else {
                                handler.post(selectUserFlairListener::success);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    selectUserFlairListener.failed(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                selectUserFlairListener.failed(throwable.getMessage());
            }
        });
    }
}
