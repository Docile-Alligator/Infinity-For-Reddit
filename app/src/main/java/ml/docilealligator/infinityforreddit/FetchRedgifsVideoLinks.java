package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;
import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.apis.RedgifsAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchRedgifsVideoLinks {

    public interface FetchRedgifsVideoLinksListener {
        void success(String webm, String mp4);
        void failed(int errorCode);
    }

    public static void fetchRedgifsVideoLinks(Executor executor, Handler handler, Retrofit redgifsRetrofit,
                                              SharedPreferences currentAccountSharedPreferences,
                                              String redgifsId,
                                              FetchRedgifsVideoLinksListener fetchRedgifsVideoLinksListener) {
        executor.execute(() -> {
            try {
                Response<String> response = redgifsRetrofit.create(RedgifsAPI.class).getRedgifsData(APIUtils.getRedgifsOAuthHeader(currentAccountSharedPreferences.getString(SharedPreferencesUtils.REDGIFS_ACCESS_TOKEN, "")),
                         redgifsId, APIUtils.USER_AGENT).execute();
                if (response.isSuccessful()) {
                    parseRedgifsVideoLinks(handler, response.body(), fetchRedgifsVideoLinksListener);
                } else {
                    handler.post(() -> fetchRedgifsVideoLinksListener.failed(response.code()));
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> fetchRedgifsVideoLinksListener.failed(-1));
            }
        });
    }

    public static void fetchRedgifsVideoLinksInRecyclerViewAdapter(Executor executor, Handler handler,
                                                                   Call<String> redgifsCall,
                                                                   FetchRedgifsVideoLinksListener fetchRedgifsVideoLinksListener) {
        executor.execute(() -> {
            try {
                Response<String> response = redgifsCall.execute();
                if (response.isSuccessful()) {
                    parseRedgifsVideoLinks(handler, response.body(), fetchRedgifsVideoLinksListener);
                } else {
                    handler.post(() -> fetchRedgifsVideoLinksListener.failed(response.code()));
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> fetchRedgifsVideoLinksListener.failed(-1));
            }
        });
    }

    private static void parseRedgifsVideoLinks(Handler handler, String response,
                                              FetchRedgifsVideoLinksListener fetchRedgifsVideoLinksListener) {
        try {
            String mp4 = new JSONObject(response).getJSONObject(JSONUtils.GIF_KEY).getJSONObject(JSONUtils.URLS_KEY)
                    .getString(JSONUtils.HD_KEY);
            handler.post(() -> fetchRedgifsVideoLinksListener.success(mp4, mp4));
        } catch (JSONException e) {
            e.printStackTrace();
            handler.post(() -> fetchRedgifsVideoLinksListener.failed(-1));
        }
    }
}
