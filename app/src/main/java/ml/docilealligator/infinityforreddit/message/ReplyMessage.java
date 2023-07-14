package ml.docilealligator.infinityforreddit.message;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ReplyMessage {
    public static void replyMessage(String messageMarkdown, String thingFullname,
                                    Locale locale, Retrofit oauthRetrofit, String accessToken,
                                    ReplyMessageListener replyMessageListener) {
        Map<String, String> headers = APIUtils.getOAuthHeader(accessToken);
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.API_TYPE_KEY, "json");
        params.put(APIUtils.RETURN_RTJSON_KEY, "true");
        params.put(APIUtils.TEXT_KEY, messageMarkdown);
        params.put(APIUtils.THING_ID_KEY, thingFullname);

        oauthRetrofit.create(RedditAPI.class).sendCommentOrReplyToMessage(headers, params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseMessage.parseRepliedMessage(response.body(), locale, new ParseMessage.ParseSentMessageAsyncTaskListener() {
                        @Override
                        public void parseSuccess(Message message) {
                            replyMessageListener.replyMessageSuccess(message);
                        }

                        @Override
                        public void parseFailed(String errorMessage) {
                            replyMessageListener.replyMessageFailed(errorMessage);
                        }
                    });
                } else {
                    replyMessageListener.replyMessageFailed(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                replyMessageListener.replyMessageFailed(t.getMessage());
            }
        });
    }

    public interface ReplyMessageListener {
        void replyMessageSuccess(Message message);
        void replyMessageFailed(String errorMessage);
    }
}
