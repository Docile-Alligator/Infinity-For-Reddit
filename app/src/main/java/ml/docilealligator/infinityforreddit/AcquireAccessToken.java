package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by alex on 3/13/18.
 */

class AcquireAccessToken {

    interface AcquireAccessTokenListener {
        void onAcquireAccessTokenSuccess();
        void onAcquireAccessTokenFail();
    }

    private Context mContext;
    private AcquireAccessTokenListener mAcquireAccessTokenListener;

    AcquireAccessToken(Context context) {
        mContext = context;
    }

    void refreshAccessToken(RequestQueue refreshQueue, AcquireAccessTokenListener acquireAccessTokenListener) {
        if(mContext != null) {
            mAcquireAccessTokenListener = acquireAccessTokenListener;
            final String refreshToken = mContext.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.REFRESH_TOKEN_KEY, "");
            StringRequest newTokenRequest = new StringRequest(Request.Method.POST, RedditUtils.ACQUIRE_ACCESS_TOKEN_URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String newAccessToken = jsonObject.getString(RedditUtils.ACCESS_TOKEN_KEY);

                        SharedPreferences.Editor editor = mContext.getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).edit();
                        editor.putString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, newAccessToken);
                        editor.apply();

                        mAcquireAccessTokenListener.onAcquireAccessTokenSuccess();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        mAcquireAccessTokenListener.onAcquireAccessTokenFail();
                        Toast.makeText(mContext, "Error parsing JSON object when getting the access token", Toast.LENGTH_SHORT).show();
                        Log.i("main activity", "Error parsing JSON object when getting the access token");
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(mContext, "Error getting the new access token", Toast.LENGTH_SHORT).show();
                    mAcquireAccessTokenListener.onAcquireAccessTokenFail();
                    Log.i("error get access token", error.getMessage());
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put(RedditUtils.GRANT_TYPE_KEY, RedditUtils.GRANT_TYPE_REFRESH_TOKEN);
                    params.put(RedditUtils.REFRESH_TOKEN_KEY, refreshToken);
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() {
                    return RedditUtils.getHttpBasicAuthHeader();
                }
            };
            newTokenRequest.setTag(AcquireAccessToken.class);
            refreshQueue.add(newTokenRequest);
        }
    }
}
