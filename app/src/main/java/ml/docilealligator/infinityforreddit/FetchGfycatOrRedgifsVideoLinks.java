package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import ml.docilealligator.infinityforreddit.API.GfycatAPI;
import ml.docilealligator.infinityforreddit.Utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchGfycatOrRedgifsVideoLinks {
    public interface FetchGfycatOrRedgifsVideoLinksListener {
        void success(String webm, String mp4);
        void failed(int errorCode);
    }

    public static void fetchGfycatOrRedgifsVideoLinks(Retrofit gfycatRetrofit, String gfycatId,
                                                      FetchGfycatOrRedgifsVideoLinksListener fetchGfycatOrRedgifsVideoLinksListener) {
        gfycatRetrofit.create(GfycatAPI.class).getGfycatData(gfycatId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new ParseGfycatVideoLinksAsyncTask(response.body(), fetchGfycatOrRedgifsVideoLinksListener).execute();
                } else {
                    fetchGfycatOrRedgifsVideoLinksListener.failed(response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchGfycatOrRedgifsVideoLinksListener.failed(-1);
            }
        });
    }

    private static class ParseGfycatVideoLinksAsyncTask extends AsyncTask<Void, Void, Void> {

        private String response;
        private String webm;
        private String mp4;
        private boolean parseFailed = false;
        private FetchGfycatOrRedgifsVideoLinksListener fetchGfycatOrRedgifsVideoLinksListener;

        ParseGfycatVideoLinksAsyncTask(String response, FetchGfycatOrRedgifsVideoLinksListener fetchGfycatOrRedgifsVideoLinksListener) {
            this.response = response;
            this.fetchGfycatOrRedgifsVideoLinksListener = fetchGfycatOrRedgifsVideoLinksListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                webm = jsonObject.getJSONObject(JSONUtils.GFY_ITEM_KEY).getString(JSONUtils.WEBM_URL_KEY);
                mp4 = jsonObject.getJSONObject(JSONUtils.GFY_ITEM_KEY).getString(JSONUtils.MP4_URL_KEY);
            } catch (JSONException e) {
                e.printStackTrace();
                parseFailed = true;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (parseFailed) {
                fetchGfycatOrRedgifsVideoLinksListener.failed(-1);
            } else {
                fetchGfycatOrRedgifsVideoLinksListener.success(webm, mp4);
            }
        }
    }
}
