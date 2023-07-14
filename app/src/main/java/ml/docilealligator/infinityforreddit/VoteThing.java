package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

/**
 * Created by alex on 3/14/18.
 */

public class VoteThing {

    public static void voteThing(Context context, final Retrofit retrofit, String accessToken,
                                 final VoteThingListener voteThingListener, final String fullName,
                                 final String point, final int position) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.DIR_KEY, point);
        params.put(APIUtils.ID_KEY, fullName);
        params.put(APIUtils.RANK_KEY, APIUtils.RANK);

        Call<String> voteThingCall = api.voteThing(APIUtils.getOAuthHeader(accessToken), params);
        voteThingCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    voteThingListener.onVoteThingSuccess(position);
                } else {
                    voteThingListener.onVoteThingFail(position);
                    Toast.makeText(context, "Code " + response.code() + " Body: " + response.body(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                voteThingListener.onVoteThingFail(position);
                Toast.makeText(context, "Network error " + "Body: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void voteThing(Context context, final Retrofit retrofit, String accessToken,
                                 final VoteThingWithoutPositionListener voteThingWithoutPositionListener,
                                 final String fullName, final String point) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.DIR_KEY, point);
        params.put(APIUtils.ID_KEY, fullName);
        params.put(APIUtils.RANK_KEY, APIUtils.RANK);

        Call<String> voteThingCall = api.voteThing(APIUtils.getOAuthHeader(accessToken), params);
        voteThingCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    voteThingWithoutPositionListener.onVoteThingSuccess();
                } else {
                    voteThingWithoutPositionListener.onVoteThingFail();
                    Toast.makeText(context, "Code " + response.code() + " Body: " + response.body(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                voteThingWithoutPositionListener.onVoteThingFail();
                Toast.makeText(context, "Network error " + "Body: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public interface VoteThingListener {
        void onVoteThingSuccess(int position);

        void onVoteThingFail(int position);
    }

    public interface VoteThingWithoutPositionListener {
        void onVoteThingSuccess();

        void onVoteThingFail();
    }
}
