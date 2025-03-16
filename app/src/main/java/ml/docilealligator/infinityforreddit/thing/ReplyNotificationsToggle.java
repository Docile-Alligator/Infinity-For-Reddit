package ml.docilealligator.infinityforreddit.thing;

import android.os.Handler;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ReplyNotificationsToggle {
    public static void toggleEnableNotification(Handler handler, Retrofit oauthRetrofit, String accessToken,
                                                Comment comment, SendNotificationListener sendNotificationListener) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, comment.getFullName());
        params.put(APIUtils.STATE_KEY, String.valueOf(!comment.isSendReplies()));
        oauthRetrofit.create(RedditAPI.class).toggleRepliesNotification(APIUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    handler.post(sendNotificationListener::onSuccess);
                } else {
                    handler.post(sendNotificationListener::onError);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                handler.post(sendNotificationListener::onError);
            }
        });
    }

    public interface SendNotificationListener {
        void onSuccess();
        void onError();
    }
}
