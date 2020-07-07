package ml.docilealligator.infinityforreddit.Comment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ml.docilealligator.infinityforreddit.API.RedditAPI;
import ml.docilealligator.infinityforreddit.Utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SendComment {

    public static void sendComment(String commentMarkdown, String thingFullname, int parentDepth,
                                   Locale locale, Retrofit oauthRetrofit, String accessToken,
                                   SendCommentListener sendCommentListener) {
        Map<String, String> headers = APIUtils.getOAuthHeader(accessToken);
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.API_TYPE_KEY, "json");
        params.put(APIUtils.RETURN_RTJSON_KEY, "true");
        params.put(APIUtils.TEXT_KEY, commentMarkdown);
        params.put(APIUtils.THING_ID_KEY, thingFullname);

        oauthRetrofit.create(RedditAPI.class).sendCommentOrReplyToMessage(headers, params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseComment.parseSentComment(response.body(), parentDepth, locale, new ParseComment.ParseSentCommentListener() {
                        @Override
                        public void onParseSentCommentSuccess(Comment comment) {
                            sendCommentListener.sendCommentSuccess(comment);
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

    public interface SendCommentListener {
        void sendCommentSuccess(Comment comment);

        void sendCommentFailed(String errorMessage);
    }
}
