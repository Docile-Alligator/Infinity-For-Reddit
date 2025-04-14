package ml.docilealligator.infinityforreddit.message;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchMessage {

    public static final String WHERE_INBOX = "inbox";
    public static final String WHERE_UNREAD = "unread";
    public static final String WHERE_SENT = "sent";
    public static final String WHERE_COMMENTS = "comments";
    public static final String WHERE_MESSAGES = "messages";
    public static final String WHERE_MESSAGES_DETAIL = "messages_detail";
    public static final int MESSAGE_TYPE_INBOX = 0;
    public static final int MESSAGE_TYPE_PRIVATE_MESSAGE = 1;
    public static final int MESSAGE_TYPE_NOTIFICATION = 2;

    static void fetchInbox(Executor executor, Handler handler,  Retrofit oauthRetrofit, Locale locale, String accessToken, String where,
                           String after, int messageType, FetchMessagesListener fetchMessagesListener) {
        oauthRetrofit.create(RedditAPI.class).getMessages(APIUtils.getOAuthHeader(accessToken), where, after).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> {
                        try {
                            JSONObject jsonResponse = new JSONObject(response.body());
                            JSONArray messageArray = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.CHILDREN_KEY);
                            List<Message> messages = ParseMessage.parseMessages(messageArray, locale, messageType);
                            String newAfter = jsonResponse.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.AFTER_KEY);
                            handler.post(() -> fetchMessagesListener.fetchSuccess(messages, newAfter));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            handler.post(fetchMessagesListener::fetchFailed);
                        }
                    });
                } else {
                    fetchMessagesListener.fetchFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                fetchMessagesListener.fetchFailed();
            }
        });
    }

    interface FetchMessagesListener {
        void fetchSuccess(List<Message> messages, @Nullable String after);

        void fetchFailed();
    }
}
