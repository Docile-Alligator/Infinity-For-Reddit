package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.Utils.RedditUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ReportThing {

    public interface ReportThingListener {
        void success();
        void failed();
    }

    public static void reportThing(Retrofit oauthRetrofit, String accessToken, String thingFullname,
                                   String subredditName, String reasonType, String reason,
                                   ReportThingListener reportThingListener) {
        Map<String, String> header = RedditUtils.getOAuthHeader(accessToken);
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.THING_ID_KEY, thingFullname);
        params.put(RedditUtils.SR_NAME_KEY, subredditName);
        params.put(reasonType, reason);

        oauthRetrofit.create(RedditAPI.class).report(header, params).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    reportThingListener.success();
                } else {
                    reportThingListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                reportThingListener.failed();
            }
        });
    }
}