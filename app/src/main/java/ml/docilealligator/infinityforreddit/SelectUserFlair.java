package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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

    public static void selectUserFlair(Retrofit oauthRetrofit, String accessToken, UserFlair userFlair,
                                       String subredditName, String accountName, SelectUserFlairListener selectUserFlairListener) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.API_TYPE_KEY, APIUtils.API_TYPE_JSON);
        params.put(APIUtils.FLAIR_TEMPLATE_ID_KEY, userFlair.getId());
        params.put(APIUtils.NAME_KEY, accountName);
        params.put(APIUtils.TEXT_KEY, userFlair.getText());
        oauthRetrofit.create(RedditAPI.class).selectUserFlair(APIUtils.getOAuthHeader(accessToken), params, subredditName)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            new ParseErrorAsyncTask(response.body(), selectUserFlairListener).execute();
                        } else {
                            selectUserFlairListener.failed(response.message());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        selectUserFlairListener.failed(t.getMessage());
                    }
                });
    }

    private static class ParseErrorAsyncTask extends AsyncTask<Void, Void, Void> {

        private String response;
        @Nullable
        private String errorMessage;
        private SelectUserFlairListener selectUserFlairListener;

        interface ParseErrorAsyncTaskListener {
            void parseFinished(@Nullable String errorMessage);
        }

        ParseErrorAsyncTask(String response, SelectUserFlairListener selectUserFlairListener) {
            this.response = response;
            this.selectUserFlairListener = selectUserFlairListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject responseObject = new JSONObject(response).getJSONObject(JSONUtils.JSON_KEY);

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
                        errorMessage = errorString.substring(0, 1).toUpperCase() + errorString.substring(1);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (errorMessage == null) {
                selectUserFlairListener.success();
            } else {
                selectUserFlairListener.failed(errorMessage);
            }
        }
    }
}
