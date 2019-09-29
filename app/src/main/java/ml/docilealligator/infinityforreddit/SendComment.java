package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SendComment {

    public interface SendCommentListener {
        void sendCommentSuccess(CommentData commentData);
        void sendCommentFailed(String errorMessage);
    }

    public static void sendComment(String commentMarkdown, String thingFullname, int parentDepth,
                                   Locale locale, Retrofit oauthRetrofit, String accessToken,
                                   SendCommentListener sendCommentListener) {
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
                    ParseComment.parseSentComment(response.body(), parentDepth, locale, new ParseComment.ParseSentCommentListener() {
                        @Override
                        public void onParseSentCommentSuccess(CommentData commentData) {
                            sendCommentListener.sendCommentSuccess(commentData);
                        }

                        @Override
                        public void onParseSentCommentFailed(@Nullable String errorMessage) {
                            sendCommentListener.sendCommentFailed(errorMessage);
                        }
                    });
                } else {
                    sendCommentListener.sendCommentFailed(response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                sendCommentListener.sendCommentFailed(t.getMessage());
            }
        });
    }
}
