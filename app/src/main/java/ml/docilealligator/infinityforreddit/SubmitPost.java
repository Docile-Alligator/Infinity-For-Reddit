package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

class SubmitPost {
    interface SubmitPostListener {
        void submitSuccessful(Post post);
        void submitFailed(@Nullable String errorMessage);
    }

    static void submitPostText(Retrofit oauthRetrofit, SharedPreferences authInfoSharedPreferences,
                            Locale locale, String subredditName, String title, String text, boolean isNSFW,
                            SubmitPostListener submitPostListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);
        String accessToken = authInfoSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");

        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.API_TYPE_KEY, RedditUtils.API_TYPE_JSON);
        params.put(RedditUtils.SR_KEY, subredditName);
        params.put(RedditUtils.TITLE_KEY, title);
        params.put(RedditUtils.KIND_KEY, RedditUtils.KIND_SELF);
        params.put(RedditUtils.TEXT_KEY, text);
        params.put(RedditUtils.NSFW_KEY, Boolean.toString(isNSFW));

        Call<String> submitPostCall = api.submit(RedditUtils.getOAuthHeader(accessToken), params);
        submitPostCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                Log.i("code", "asfd" + response.body());
                if(response.isSuccessful()) {
                    try {
                        getSubmittedPost(response.body(), oauthRetrofit, authInfoSharedPreferences, locale,
                                submitPostListener);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        submitPostListener.submitFailed(null);
                    }
                } else {
                    Log.i("call_failed", response.message());
                    submitPostListener.submitFailed(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call_failed", call.request().url().toString());
                submitPostListener.submitFailed(null);
            }
        });
    }

    private static void getSubmittedPost(String response, Retrofit oauthRetrofit,
                                         SharedPreferences authInfoSharedPreferences, Locale locale,
                                         SubmitPostListener submitPostListener) throws JSONException {
        JSONObject responseObject = new JSONObject(response).getJSONObject(JSONUtils.JSON_KEY);
        if(responseObject.getJSONArray(JSONUtils.ERRORS_KEY).length() != 0) {
            JSONArray error = responseObject.getJSONArray(JSONUtils.ERRORS_KEY)
                    .getJSONArray(responseObject.getJSONArray(JSONUtils.ERRORS_KEY).length() - 1);
            if(error.length() != 0) {
                String errorString;
                if(error.length() >= 2) {
                    errorString = error.getString(1);
                    errorString = errorString.substring(0, 1).toUpperCase() + errorString.substring(1);
                    submitPostListener.submitFailed(errorString);
                } else {
                    errorString = error.getString(0);
                    errorString = errorString.substring(0, 1).toUpperCase() + errorString.substring(1);
                    submitPostListener.submitFailed(errorString);
                }
            } else {
                submitPostListener.submitFailed(null);
            }

            return;
        }

        String postId = responseObject.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.ID_KEY);

        RedditAPI api = oauthRetrofit.create(RedditAPI.class);
        String accessToken = authInfoSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");

        Call<String> getPostCall = api.getPost(postId, RedditUtils.getOAuthHeader(accessToken));
        getPostCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePost(response.body(), locale, new ParsePost.ParsePostListener() {
                        @Override
                        public void onParsePostSuccess(Post post) {
                            submitPostListener.submitSuccessful(post);
                        }

                        @Override
                        public void onParsePostFail() {
                            submitPostListener.submitFailed(null);
                        }
                    });
                } else {
                    Log.i("call_failed", response.message());
                    submitPostListener.submitFailed(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call_failed", call.request().url().toString());
                submitPostListener.submitFailed(t.getMessage());
            }
        });
    }
}
