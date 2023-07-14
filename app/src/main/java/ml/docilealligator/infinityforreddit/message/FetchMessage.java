package ml.docilealligator.infinityforreddit.message;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
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

    static void fetchInbox(Retrofit oauthRetrofit, Locale locale, String accessToken, String where,
                           String after, int messageType, FetchMessagesListener fetchMessagesListener) {
        oauthRetrofit.create(RedditAPI.class).getMessages(APIUtils.getOAuthHeader(accessToken), where, after)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            ParseMessage.parseMessage(response.body(), locale, messageType, fetchMessagesListener::fetchSuccess);
                        } else {
                            fetchMessagesListener.fetchFailed();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        fetchMessagesListener.fetchFailed();
                    }
                });
    }

    interface FetchMessagesListener {
        void fetchSuccess(ArrayList<Message> messages, @Nullable String after);

        void fetchFailed();
    }
}
