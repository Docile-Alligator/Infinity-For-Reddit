package ml.docilealligator.infinityforreddit.message;

import android.os.Handler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ComposeMessage {
    public static void composeMessage(Executor executor, Handler handler,  Retrofit oauthRetrofit, String accessToken, Locale locale, String username,
                                      String subject, String message, ComposeMessageListener composeMessageListener) {
        executor.execute(() -> {
            Map<String, String> headers = APIUtils.getOAuthHeader(accessToken);
            Map<String, String> params = new HashMap<>();
            params.put(APIUtils.API_TYPE_KEY, "json");
            params.put(APIUtils.RETURN_RTJSON_KEY, "true");
            params.put(APIUtils.SUBJECT_KEY, subject);
            params.put(APIUtils.TEXT_KEY, message);
            params.put(APIUtils.TO_KEY, username);

            try {
                Response<String> response = oauthRetrofit.create(RedditAPI.class).composePrivateMessage(headers, params).execute();
                if (response.isSuccessful()) {
                    String errorMessage = ParseMessage.parseRepliedMessageErrorMessage(response.body());
                    if (errorMessage == null) {
                        handler.post(composeMessageListener::composeMessageSuccess);
                    } else {
                        handler.post(() -> composeMessageListener.composeMessageFailed(errorMessage));
                    }
                } else {
                    handler.post(() -> composeMessageListener.composeMessageFailed(response.message()));
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> composeMessageListener.composeMessageFailed(e.getMessage()));
            }
        });
    }

    public interface ComposeMessageListener {
        void composeMessageSuccess();
        void composeMessageFailed(String errorMessage);
    }
}
