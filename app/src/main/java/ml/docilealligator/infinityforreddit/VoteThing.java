package ml.docilealligator.infinityforreddit;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 3/14/18.
 */

class VoteThing {

    interface VoteThingListener {
        void onVoteThingSuccess(int position);
        void onVoteThingFail(int position);
    }

    private Context mContext;
    private VoteThingListener mVoteThingListener;
    private RequestQueue mQueue;
    private RequestQueue mAcquireAccessTokenRequestQueue;

    VoteThing(Context context, RequestQueue queue, RequestQueue acquireAccessTokenRequestQueue) {
        mContext = context;
        mQueue = queue;
        mAcquireAccessTokenRequestQueue = acquireAccessTokenRequestQueue;
    }

    void votePost(VoteThingListener voteThingListener, final String fullName, final String point, final int position, final int refreshTime) {
        if(mContext != null) {
            if(refreshTime < 0) {
                mVoteThingListener.onVoteThingFail(position);
                return;
            }
            mVoteThingListener = voteThingListener;
            StringRequest voteRequest = new StringRequest(Request.Method.POST, RedditUtils.OAUTH_API_BASE_URI + RedditUtils.VOTE_SUFFIX, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    mVoteThingListener.onVoteThingSuccess(position);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error instanceof AuthFailureError) {
                        //Access token expired
                        new AcquireAccessToken(mContext).refreshAccessToken(mAcquireAccessTokenRequestQueue,
                                new AcquireAccessToken.AcquireAccessTokenListener() {
                                    @Override
                                    public void onAcquireAccessTokenSuccess() {
                                        votePost(mVoteThingListener, fullName, point, position, refreshTime - 1);
                                    }

                                    @Override
                                    public void onAcquireAccessTokenFail() {}
                                });
                    } else {
                        mVoteThingListener.onVoteThingFail(position);
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    HashMap<String, String> params = new HashMap<>();
                    params.put(RedditUtils.DIR_KEY, point);
                    params.put(RedditUtils.ID_KEY, fullName);
                    params.put(RedditUtils.RANK_KEY, RedditUtils.RANK);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() {
                    String accessToken = mContext.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
                    return RedditUtils.getOAuthHeader(accessToken);
                }
            };
            voteRequest.setTag(VoteThing.class);
            mQueue.add(voteRequest);
        }
    }
}
