package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

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

    static void voteThing(final Context context, final VoteThingListener voteThingListener, final String fullName, final String point, final int position, final int refreshTime) {
        if(context != null) {
            if(refreshTime < 0) {
                voteThingListener.onVoteThingFail(position);
                return;
            }

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(RedditUtils.OAUTH_API_BASE_URI)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            RedditAPI api = retrofit.create(RedditAPI.class);

            String accessToken = context.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                    .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
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
                    RefreshAccessToken.refreshAccessToken(context,
                            new RefreshAccessToken.RefreshAccessTokenListener() {
                                @Override
                                public void onRefreshAccessTokenSuccess() {
                                    voteThing(context, voteThingListener, fullName, point, position, refreshTime - 1);
                                }

                                @Override
                                public void onRefreshAccessTokenFail() {
                                }
                            });
                }
            });
        }
    }

    static void voteThing(final Context context, final VoteThingWithoutPositionListener voteThingWithoutPositionListener, final String fullName, final String point, final int refreshTime) {
        if(context != null) {
            if(refreshTime < 0) {
                voteThingWithoutPositionListener.onVoteThingFail();
                return;
            }

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(RedditUtils.OAUTH_API_BASE_URI)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();

            RedditAPI api = retrofit.create(RedditAPI.class);

            String accessToken = context.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                    .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
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
                    RefreshAccessToken.refreshAccessToken(context,
                            new RefreshAccessToken.RefreshAccessTokenListener() {
                                @Override
                                public void onRefreshAccessTokenSuccess() {
                                    voteThing(context, voteThingWithoutPositionListener, fullName, point, refreshTime - 1);
                                }

                                @Override
                                public void onRefreshAccessTokenFail() {}
                            });
                }
            });
        }
    }
}
