package ml.docilealligator.infinityforreddit;

import android.os.Handler;

import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;

import javax.inject.Provider;

import ml.docilealligator.infinityforreddit.apis.StreamableAPI;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Response;

public class FetchStreamableVideo {
    public interface FetchStreamableVideoListener {
        void success(StreamableVideo streamableVideo);
        void failed();
    }

    public static void fetchStreamableVideo(Executor executor, Handler handler, Provider<StreamableAPI> streamableApiProvider,
                                            String videoUrl, FetchStreamableVideoListener fetchStreamableVideoListener) {
        executor.execute(() -> {
            try {
                Response<String> response = streamableApiProvider.get().getStreamableData(videoUrl).execute();
                if (response.isSuccessful()) {
                    JSONObject jsonObject = new JSONObject(response.body());
                    String title = jsonObject.getString(JSONUtils.TITLE_KEY);
                    JSONObject filesObject = jsonObject.getJSONObject(JSONUtils.FILES_KEY);
                    StreamableVideo.Media mp4 = parseMedia(filesObject.getJSONObject(JSONUtils.MP4_KEY));
                    StreamableVideo.Media mp4MobileTemp = null;
                    if (filesObject.has(JSONUtils.MP4_MOBILE_KEY)) {
                        mp4MobileTemp = parseMedia(filesObject.getJSONObject(JSONUtils.MP4_MOBILE_KEY));
                    }
                    if (mp4 == null && mp4MobileTemp == null) {
                        handler.post(fetchStreamableVideoListener::failed);
                        return;
                    }
                    StreamableVideo.Media mp4Mobile = mp4MobileTemp;
                    handler.post(() -> fetchStreamableVideoListener.success(new StreamableVideo(title, mp4, mp4Mobile)));
                } else {
                    handler.post(fetchStreamableVideoListener::failed);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                handler.post(fetchStreamableVideoListener::failed);
            }
        });
    }

    public static void fetchStreamableVideoInRecyclerViewAdapter(Executor executor, Handler handler, Call<String> streamableCall,
                                                                 FetchStreamableVideoListener fetchStreamableVideoListener) {
        executor.execute(() -> {
            try {
                Response<String> response = streamableCall.execute();
                if (response.isSuccessful()) {
                    JSONObject jsonObject = new JSONObject(response.body());
                    String title = jsonObject.getString(JSONUtils.TITLE_KEY);
                    JSONObject filesObject = jsonObject.getJSONObject(JSONUtils.FILES_KEY);
                    StreamableVideo.Media mp4 = parseMedia(filesObject.getJSONObject(JSONUtils.MP4_KEY));
                    StreamableVideo.Media mp4MobileTemp = null;
                    if (filesObject.has(JSONUtils.MP4_MOBILE_KEY)) {
                        mp4MobileTemp = parseMedia(filesObject.getJSONObject(JSONUtils.MP4_MOBILE_KEY));
                    }
                    if (mp4 == null && mp4MobileTemp == null) {
                        handler.post(fetchStreamableVideoListener::failed);
                        return;
                    }
                    StreamableVideo.Media mp4Mobile = mp4MobileTemp;
                    handler.post(() -> fetchStreamableVideoListener.success(new StreamableVideo(title, mp4, mp4Mobile)));
                } else {
                    handler.post(fetchStreamableVideoListener::failed);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                handler.post(fetchStreamableVideoListener::failed);
            }
        });
    }

    @Nullable
    private static StreamableVideo.Media parseMedia(JSONObject jsonObject) {
        try {
            return new StreamableVideo.Media(
                    jsonObject.getString(JSONUtils.URL_KEY),
                    jsonObject.getInt(JSONUtils.WIDTH_KEY),
                    jsonObject.getInt(JSONUtils.HEIGHT_KEY));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
