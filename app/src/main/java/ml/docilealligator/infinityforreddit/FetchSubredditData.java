package ml.docilealligator.infinityforreddit;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

class FetchSubredditData {
    interface FetchSubredditDataListener {
        void onFetchSubredditDataSuccess(String response);
        void onFetchSubredditDataFail();
    }

    private RequestQueue requestQueue;
    private String subredditName;
    private FetchSubredditDataListener mFetchSubredditDataListener;

    FetchSubredditData(RequestQueue requestQueue, String subredditName) {
        this.requestQueue = requestQueue;
        this.subredditName = subredditName;
    }

    void querySubredditData(FetchSubredditDataListener fetchSubredditDataListener) {
        mFetchSubredditDataListener = fetchSubredditDataListener;
        StringRequest commentRequest = new StringRequest(Request.Method.GET, RedditUtils.getQuerySubredditDataUrl(subredditName), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                mFetchSubredditDataListener.onFetchSubredditDataSuccess(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mFetchSubredditDataListener.onFetchSubredditDataFail();
            }
        }) {};
        commentRequest.setTag(FetchSubredditData.class);
        requestQueue.add(commentRequest);
    }
}
