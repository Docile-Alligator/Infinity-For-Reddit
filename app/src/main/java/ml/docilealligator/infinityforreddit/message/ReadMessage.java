package ml.docilealligator.infinityforreddit.message;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ReadMessage {
    public static void readMessage(Retrofit oauthRetrofit, String accessToken, String commaSeparatedFullnames,
                                   ReadMessageListener readMessageListener) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, commaSeparatedFullnames);
        oauthRetrofit.create(RedditAPI.class).readMessage(APIUtils.getOAuthHeader(accessToken), params)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            readMessageListener.readSuccess();
                        } else {
                            readMessageListener.readFailed();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        readMessageListener.readFailed();
                    }
                });
    }

    public interface ReadMessageListener {
        void readSuccess();

        void readFailed();
    }
}
