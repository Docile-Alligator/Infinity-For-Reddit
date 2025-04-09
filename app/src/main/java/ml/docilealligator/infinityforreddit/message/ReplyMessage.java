package ml.docilealligator.infinityforreddit.message;

import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ReplyMessage {
    public static void replyMessage(Executor executor, Handler handler, String messageMarkdown, String thingFullname,
                                    Locale locale, Retrofit oauthRetrofit, String accessToken,
                                    ReplyMessageListener replyMessageListener) {
        executor.execute(() -> {
            Map<String, String> headers = APIUtils.getOAuthHeader(accessToken);
            Map<String, String> params = new HashMap<>();
            params.put(APIUtils.API_TYPE_KEY, "json");
            params.put(APIUtils.RETURN_RTJSON_KEY, "true");
            params.put(APIUtils.TEXT_KEY, messageMarkdown);
            params.put(APIUtils.THING_ID_KEY, thingFullname);

            try {
                Response<String> response = oauthRetrofit.create(RedditAPI.class).sendCommentOrReplyToMessage(headers, params).execute();
                if (response.isSuccessful()) {
                    try {
                        JSONObject messageJSON = new JSONObject(response.body()).getJSONObject(JSONUtils.JSON_KEY)
                                .getJSONObject(JSONUtils.DATA_KEY).getJSONArray(JSONUtils.THINGS_KEY).getJSONObject(0);
                        Message message = ParseMessage.parseSingleMessage(messageJSON, locale, FetchMessage.MESSAGE_TYPE_PRIVATE_MESSAGE);
                        handler.post(() -> replyMessageListener.replyMessageSuccess(message));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        handler.post(() -> replyMessageListener.replyMessageFailed(ParseMessage.parseRepliedMessageErrorMessage(response.body())));
                    }
                } else {
                    handler.post(() -> replyMessageListener.replyMessageFailed(response.message()));
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> replyMessageListener.replyMessageFailed(e.getMessage()));
            }
        });
    }

    public interface ReplyMessageListener {
        void replyMessageSuccess(Message message);
        void replyMessageFailed(String errorMessage);
    }
}
