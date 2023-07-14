package ml.docilealligator.infinityforreddit.award;

import android.os.Handler;
import android.text.Html;

import androidx.annotation.NonNull;

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

public class GiveAward {
    public interface GiveAwardListener {
        void success(String awardsHTML, int awardCount);
        void failed(int code, String message);
    }

    public static void giveAwardV2(Executor executor, Handler handler, Retrofit oauthRetrofit, String accessToken,
                                   String thingFullName, String awardId, boolean isAnonymous,
                                   GiveAwardListener giveAwardListener) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.GILD_TYPE, awardId);
        params.put(APIUtils.IS_ANONYMOUS, Boolean.toString(isAnonymous));
        params.put(APIUtils.THING_ID_KEY, thingFullName);
        oauthRetrofit.create(RedditAPI.class).awardThing(APIUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> parseResponse(handler, response.body(), new ParseResponseAsyncTaskListener() {
                        @Override
                        public void success(String awardsHTML, int awardCount) {
                            giveAwardListener.success(awardsHTML, awardCount);
                        }

                        @Override
                        public void failed(String errorMessage) {
                            giveAwardListener.failed(response.code(), response.body());
                        }
                    }));
                } else {
                    giveAwardListener.failed(response.code(), response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                giveAwardListener.failed(0, t.getMessage());
            }
        });
    }

    private static void parseResponse(Handler handler, String response, ParseResponseAsyncTaskListener parseResponseAsyncTaskListener) {
        try {
            //Check for error
            JSONObject responseObject = new JSONObject(response).getJSONObject(JSONUtils.JSON_KEY);

            if (responseObject.getJSONArray(JSONUtils.ERRORS_KEY).length() != 0) {
                JSONArray errorArray = responseObject.getJSONArray(JSONUtils.ERRORS_KEY)
                        .getJSONArray(responseObject.getJSONArray(JSONUtils.ERRORS_KEY).length() - 1);
                if (errorArray.length() != 0) {
                    String errorString;
                    if (errorArray.length() >= 2) {
                        errorString = errorArray.getString(1);
                    } else {
                        errorString = errorArray.getString(0);
                    }
                    String errorMessage = errorString.substring(0, 1).toUpperCase() + errorString.substring(1);
                    handler.post(() -> parseResponseAsyncTaskListener.failed(errorMessage));
                }
            }
        } catch (JSONException ignore) {}

        try {
            JSONArray awardingsArray = new JSONObject(response).getJSONArray(JSONUtils.ALL_AWARDINGS_KEY);
            StringBuilder awardingsBuilder = new StringBuilder();
            int awardCount = 0;
            for (int i = 0; i < awardingsArray.length(); i++) {
                JSONObject award = awardingsArray.getJSONObject(i);
                int count = award.getInt(JSONUtils.COUNT_KEY);
                awardCount += count;
                String iconUrl = award.getString(JSONUtils.ICON_URL_KEY);
                awardingsBuilder.append("<img src=\"").append(Html.escapeHtml(iconUrl)).append("\"> ").append("x").append(count).append(" ");
            }

            String awardsHTML = awardingsBuilder.toString();
            int finalAwardCount = awardCount;
            handler.post(() -> parseResponseAsyncTaskListener.success(awardsHTML, finalAwardCount));
        } catch (JSONException e) {
            e.printStackTrace();
            handler.post(() -> parseResponseAsyncTaskListener.failed(null));
        }
    }

    interface ParseResponseAsyncTaskListener {
        void success(String awardsHTML, int awardCount);
        void failed(String errorMessage);
    }
}
