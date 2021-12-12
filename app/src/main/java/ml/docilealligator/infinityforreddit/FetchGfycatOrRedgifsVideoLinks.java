package ml.docilealligator.infinityforreddit;

import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.apis.GfycatAPI;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchGfycatOrRedgifsVideoLinks {
    private FetchGfycatOrRedgifsVideoLinksListener fetchGfycatOrRedgifsVideoLinksListener;
    Call<String> gfycatCall;

    public interface FetchGfycatOrRedgifsVideoLinksListener {
        void success(String webm, String mp4);
        void failed(int errorCode);
    }

    public FetchGfycatOrRedgifsVideoLinks(FetchGfycatOrRedgifsVideoLinksListener fetchGfycatOrRedgifsVideoLinksListener) {
        this.fetchGfycatOrRedgifsVideoLinksListener = fetchGfycatOrRedgifsVideoLinksListener;
    }

    public static void fetchGfycatOrRedgifsVideoLinks(Executor executor, Handler handler, Retrofit gfycatRetrofit,
                                                      String gfycatId,
                                                      FetchGfycatOrRedgifsVideoLinksListener fetchGfycatOrRedgifsVideoLinksListener) {
        executor.execute(() -> {
            try {
                Response<String> response = gfycatRetrofit.create(GfycatAPI.class).getGfycatData(gfycatId).execute();
                if (response.isSuccessful()) {
                    parseGfycatVideoLinks(handler, response.body(), fetchGfycatOrRedgifsVideoLinksListener);
                } else {
                    handler.post(() -> fetchGfycatOrRedgifsVideoLinksListener.failed(response.code()));
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> fetchGfycatOrRedgifsVideoLinksListener.failed(-1));
            }
        });

    }

    public void fetchGfycatOrRedgifsVideoLinksInRecyclerViewAdapter(Executor executor, Handler handler,
                                                                    Retrofit gfycatRetrofit, Retrofit redgifsRetrofit,
                                                                    String gfycatId, boolean isGfycatVideo,
                                                                    boolean automaticallyTryRedgifs) {
        executor.execute(() -> {
            gfycatCall = (isGfycatVideo ? gfycatRetrofit : redgifsRetrofit).create(GfycatAPI.class).getGfycatData(gfycatId);
            try {
                Response<String> response = gfycatCall.execute();
                if (response.isSuccessful()) {
                    parseGfycatVideoLinks(handler, response.body(), fetchGfycatOrRedgifsVideoLinksListener);
                } else {
                    if (response.code() == 404 && isGfycatVideo && automaticallyTryRedgifs) {
                        fetchGfycatOrRedgifsVideoLinksInRecyclerViewAdapter(executor, handler, gfycatRetrofit,
                                redgifsRetrofit, gfycatId, false, false);
                    } else {
                        handler.post(() -> fetchGfycatOrRedgifsVideoLinksListener.failed(response.code()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                fetchGfycatOrRedgifsVideoLinksListener.failed(-1);
            }
        });
    }

    public void cancel() {
        if (gfycatCall != null && !gfycatCall.isCanceled()) {
            gfycatCall.cancel();
        }
    }

    private static void parseGfycatVideoLinks(Handler handler, String response,
                                              FetchGfycatOrRedgifsVideoLinksListener fetchGfycatOrRedgifsVideoLinksListener) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String mp4 = jsonObject.getJSONObject(JSONUtils.GFY_ITEM_KEY).has(JSONUtils.MP4_URL_KEY) ?
                    jsonObject.getJSONObject(JSONUtils.GFY_ITEM_KEY).getString(JSONUtils.MP4_URL_KEY)
                    : jsonObject.getJSONObject(JSONUtils.GFY_ITEM_KEY)
                    .getJSONObject(JSONUtils.CONTENT_URLS_KEY)
                    .getJSONObject(JSONUtils.MP4_KEY)
                    .getString(JSONUtils.URL_KEY);
            String webm;
            if (jsonObject.getJSONObject(JSONUtils.GFY_ITEM_KEY).has(JSONUtils.WEBM_URL_KEY)) {
                webm = jsonObject.getJSONObject(JSONUtils.GFY_ITEM_KEY).getString(JSONUtils.WEBM_URL_KEY);
            } else if (jsonObject.getJSONObject(JSONUtils.GFY_ITEM_KEY).getJSONObject(JSONUtils.CONTENT_URLS_KEY).has(JSONUtils.WEBM_KEY)) {
                webm = jsonObject.getJSONObject(JSONUtils.GFY_ITEM_KEY)
                        .getJSONObject(JSONUtils.CONTENT_URLS_KEY)
                        .getJSONObject(JSONUtils.WEBM_KEY)
                        .getString(JSONUtils.URL_KEY);
            } else {
                webm = mp4;
            }
            handler.post(() -> fetchGfycatOrRedgifsVideoLinksListener.success(webm, mp4));
        } catch (JSONException e) {
            e.printStackTrace();
            handler.post(() -> fetchGfycatOrRedgifsVideoLinksListener.failed(-1));
        }
    }
}
