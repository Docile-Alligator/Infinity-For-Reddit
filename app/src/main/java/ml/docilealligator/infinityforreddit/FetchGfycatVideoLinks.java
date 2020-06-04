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

public class FetchGfycatVideoLinks {
    public interface FetchGfycatVideoLinksListener {
        void success(String webm, String mp4);
        void failed();
    }

    public static void fetchGfycatVideoLinks(Retrofit gfycatRetrofit, String gfycatId,
                                             FetchGfycatVideoLinksListener fetchGfycatVideoLinksListener) {
        gfycatRetrofit.create(GfycatAPI.class).getGfycatData(gfycatId).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new ParseGfycatVideoLinksAsyncTask(response.body(), fetchGfycatVideoLinksListener).execute();
                } else {
                    fetchGfycatVideoLinksListener.failed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchGfycatVideoLinksListener.failed();
            }
        });
    }

    private static class ParseGfycatVideoLinksAsyncTask extends AsyncTask<Void, Void, Void> {

        private String response;
        private String webm;
        private String mp4;
        private boolean parseFailed = false;
        private FetchGfycatVideoLinksListener fetchGfycatVideoLinksListener;

        ParseGfycatVideoLinksAsyncTask(String response, FetchGfycatVideoLinksListener fetchGfycatVideoLinksListener) {
            this.response = response;
            this.fetchGfycatVideoLinksListener = fetchGfycatVideoLinksListener;
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
                fetchGfycatVideoLinksListener.failed();
            } else {
                fetchGfycatVideoLinksListener.success(webm, mp4);
            }
        }
    }
}
