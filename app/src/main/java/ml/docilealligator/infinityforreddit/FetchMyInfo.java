package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.API.RedditAPI;
import ml.docilealligator.infinityforreddit.Utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class FetchMyInfo {

    public static void fetchAccountInfo(final Retrofit retrofit, String accessToken,
                                        final FetchUserMyListener fetchUserMyListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> userInfo = api.getMyInfo(APIUtils.getOAuthHeader(accessToken));
        userInfo.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    fetchUserMyListener.onFetchMyInfoSuccess(response.body());
                } else {
                    fetchUserMyListener.onFetchMyInfoFail();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchUserMyListener.onFetchMyInfoFail();
            }
        });
    }

    public interface FetchUserMyListener {
        void onFetchMyInfoSuccess(String response);

        void onFetchMyInfoFail();
    }
}
