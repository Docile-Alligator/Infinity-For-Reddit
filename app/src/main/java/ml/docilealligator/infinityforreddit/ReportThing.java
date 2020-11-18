package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
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
        Map<String, String> header = APIUtils.getOAuthHeader(accessToken);
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.THING_ID_KEY, thingFullname);
        params.put(APIUtils.SR_NAME_KEY, subredditName);
        params.put(reasonType, reason);
        if (reasonType.equals(ReportReason.REASON_TYPE_SITE_REASON)) {
            params.put(APIUtils.REASON_KEY, ReportReason.REASON_SITE_REASON_SELECTED);
        } else if (reasonType.equals(ReportReason.REASON_TYPE_RULE_REASON)) {
            params.put(APIUtils.REASON_KEY, ReportReason.REASON_RULE_REASON_SELECTED);
        } else {
            params.put(APIUtils.REASON_KEY, ReportReason.REASON_OTHER);
        }
        params.put(APIUtils.API_TYPE_KEY, APIUtils.API_TYPE_JSON);

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