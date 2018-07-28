package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.net.Uri;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.Map;

class FetchUserInfo {
    interface FetchUserInfoListener {
        void onFetchUserInfoSuccess(String response);
        void onFetchUserInfoFail();
    }

    private Context context;
    private RequestQueue requestQueue;
    private FetchUserInfoListener mFetchUserInfoListener;

    FetchUserInfo(Context context, RequestQueue requestQueue) {
        this.context = context;
        this.requestQueue = requestQueue;
    }

    void queryUserInfo(FetchUserInfoListener fetchUserInfoListener, final int refreshTime) {
        if(refreshTime < 0) {
            mFetchUserInfoListener.onFetchUserInfoFail();
            return;
        }

        mFetchUserInfoListener = fetchUserInfoListener;

        Uri uri = Uri.parse(RedditUtils.OAUTH_API_BASE_URI + RedditUtils.USER_INFO_SUFFIX)
                .buildUpon().appendQueryParameter(RedditUtils.RAW_JSON_KEY, RedditUtils.RAW_JSON_VALUE)
                .build();

        StringRequest commentRequest = new StringRequest(Request.Method.GET, uri.toString(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mFetchUserInfoListener.onFetchUserInfoSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error instanceof AuthFailureError) {
                    new AcquireAccessToken(context).refreshAccessToken(requestQueue, new AcquireAccessToken.AcquireAccessTokenListener() {
                        @Override
                        public void onAcquireAccessTokenSuccess() {
                            queryUserInfo(mFetchUserInfoListener, refreshTime - 1);
                        }

                        @Override
                        public void onAcquireAccessTokenFail() {}
                    });
                } else {
                    mFetchUserInfoListener.onFetchUserInfoFail();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                String accessToken = context.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
                return RedditUtils.getOAuthHeader(accessToken);
            }
        };
        commentRequest.setTag(FetchComment.class);
        requestQueue.add(commentRequest);
    }
}
