package ml.docilealligator.infinityforreddit.thing;

import android.content.SharedPreferences;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.FetchVideoLinkListener;
import ml.docilealligator.infinityforreddit.apis.RedgifsAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchRedgifsVideoLinks {
    public static void fetchRedgifsVideoLinks(Executor executor, Handler handler, Retrofit redgifsRetrofit,
                                              SharedPreferences currentAccountSharedPreferences,
                                              String redgifsId,
                                              FetchVideoLinkListener fetchVideoLinkListener) {
        executor.execute(() -> {
            try {
                Response<String> response = redgifsRetrofit.create(RedgifsAPI.class).getRedgifsData(APIUtils.getRedgifsOAuthHeader(currentAccountSharedPreferences.getString(SharedPreferencesUtils.REDGIFS_ACCESS_TOKEN, "")),
                         redgifsId, APIUtils.USER_AGENT).execute();
                if (response.isSuccessful()) {
                    parseRedgifsVideoLinks(handler, response.body(), fetchVideoLinkListener);
                } else {
                    handler.post(() -> fetchVideoLinkListener.failed(null));
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> fetchVideoLinkListener.failed(null));
            }
        });
    }

    public static void fetchRedgifsVideoLinksInRecyclerViewAdapter(Executor executor, Handler handler,
                                                                   Call<String> redgifsCall,
                                                                   FetchVideoLinkListener fetchVideoLinkListener) {
        executor.execute(() -> {
            try {
                Response<String> response = redgifsCall.execute();
                if (response.isSuccessful()) {
                    parseRedgifsVideoLinks(handler, response.body(), fetchVideoLinkListener);
                } else {
                    handler.post(() -> fetchVideoLinkListener.failed(null));
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> fetchVideoLinkListener.failed(null));
            }
        });
    }

    private static void parseRedgifsVideoLinks(Handler handler, String response,
                                              FetchVideoLinkListener fetchVideoLinkListener) {
        try {
            String mp4 = new JSONObject(response).getJSONObject(JSONUtils.GIF_KEY).getJSONObject(JSONUtils.URLS_KEY)
                    .getString(JSONUtils.HD_KEY);
            handler.post(() -> fetchVideoLinkListener.onFetchRedgifsVideoLinkSuccess(mp4, mp4));
        } catch (JSONException e) {
            e.printStackTrace();
            handler.post(() -> fetchVideoLinkListener.failed(null));
        }
    }
}
