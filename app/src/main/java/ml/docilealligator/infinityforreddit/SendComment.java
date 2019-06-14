package ml.docilealligator.infinityforreddit;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class SendComment {

    interface SendCommentListener {
        void sendCommentSuccess();
        void sendCommentFailed();
    }

    static void sendComment(String commentMarkdown, String thingFullname, Retrofit oauthRetrofit,
                            String accessToken, SendCommentListener sendCommentListener) {
        RedditAPI api = oauthRetrofit.create(RedditAPI.class);
        Map<String, String> headers = RedditUtils.getOAuthHeader(accessToken);
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.API_TYPE_KEY, "json");
        params.put(RedditUtils.RETURN_RTJSON_KEY, "true");
        params.put(RedditUtils.TEXT_KEY, commentMarkdown);
        params.put(RedditUtils.THING_ID_KEY, thingFullname);
        api.sendComment(headers, params);

        Call<String> sendCommentCall = api.sendComment(headers, params);
        sendCommentCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    Log.i("asdfasdfaf", response.body());
                    sendCommentListener.sendCommentSuccess();
                } else {
                    sendCommentListener.sendCommentFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                sendCommentListener.sendCommentFailed();
            }
        });
    }
}
