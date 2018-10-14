package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

/**
 * Created by alex on 3/14/18.
 */

class VoteThing {

    interface VoteThingListener {
        void onVoteThingSuccess(int position);
        void onVoteThingFail(int position);
    }

    interface VoteThingWithoutPositionListener {
        void onVoteThingSuccess();
        void onVoteThingFail();
    }

    static void voteThing(final Retrofit retrofit, SharedPreferences authInfoSharedPreferences,
                          final VoteThingListener voteThingListener, final String fullName,
                          final String point, final int position) {
            RedditAPI api = retrofit.create(RedditAPI.class);

            String accessToken = authInfoSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
            Map<String, String> params = new HashMap<>();
            params.put(RedditUtils.DIR_KEY, point);
            params.put(RedditUtils.ID_KEY, fullName);
            params.put(RedditUtils.RANK_KEY, RedditUtils.RANK);

            Call<String> voteThingCall = api.voteThing(RedditUtils.getOAuthHeader(accessToken), params);
            voteThingCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                    voteThingListener.onVoteThingSuccess(position);
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Log.i("call failed", t.getMessage());
                    voteThingListener.onVoteThingFail(position);
                }
            });
    }

    static void voteThing(final Retrofit retrofit, SharedPreferences authInfoSharedPreferences,
                          final VoteThingWithoutPositionListener voteThingWithoutPositionListener,
                          final String fullName, final String point) {
            RedditAPI api = retrofit.create(RedditAPI.class);

            String accessToken = authInfoSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
            Map<String, String> params = new HashMap<>();
            params.put(RedditUtils.DIR_KEY, point);
            params.put(RedditUtils.ID_KEY, fullName);
            params.put(RedditUtils.RANK_KEY, RedditUtils.RANK);

            Call<String> voteThingCall = api.voteThing(RedditUtils.getOAuthHeader(accessToken), params);
            voteThingCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                    voteThingWithoutPositionListener.onVoteThingSuccess();
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    Log.i("call failed", t.getMessage());
                    voteThingWithoutPositionListener.onVoteThingFail();
                }
            });
    }
}
