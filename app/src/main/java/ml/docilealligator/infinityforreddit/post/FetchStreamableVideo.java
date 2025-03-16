package ml.docilealligator.infinityforreddit.post;

import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;

import javax.inject.Provider;

import ml.docilealligator.infinityforreddit.FetchVideoLinkListener;
import ml.docilealligator.infinityforreddit.thing.StreamableVideo;
import ml.docilealligator.infinityforreddit.apis.StreamableAPI;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Response;

public class FetchStreamableVideo {
    public static void fetchStreamableVideo(Executor executor, Handler handler, Provider<StreamableAPI> streamableApiProvider,
                                            String videoUrl, FetchVideoLinkListener fetchVideoLinkListener) {
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
                        handler.post(() -> fetchVideoLinkListener.failed(null));
                        return;
                    }
                    StreamableVideo.Media mp4Mobile = mp4MobileTemp;
                    handler.post(() -> fetchVideoLinkListener.onFetchStreamableVideoLinkSuccess(new StreamableVideo(title, mp4, mp4Mobile)));
                } else {
                    handler.post(() -> fetchVideoLinkListener.failed(null));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                handler.post(() -> fetchVideoLinkListener.failed(null));
            }
        });
    }

    @WorkerThread
    @Nullable
    public static StreamableVideo fetchStreamableVideoSync(Provider<StreamableAPI> streamableApiProvider,
                                            String videoUrl) {
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
                    return null;
                }
                StreamableVideo.Media mp4Mobile = mp4MobileTemp;
                return new StreamableVideo(title, mp4, mp4Mobile);
            } else {
                return null;
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void fetchStreamableVideoInRecyclerViewAdapter(Executor executor, Handler handler, Call<String> streamableCall,
                                                                 FetchVideoLinkListener fetchVideoLinkListener) {
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
                        handler.post(() -> fetchVideoLinkListener.failed(null));
                        return;
                    }
                    StreamableVideo.Media mp4Mobile = mp4MobileTemp;
                    handler.post(() -> fetchVideoLinkListener.onFetchStreamableVideoLinkSuccess(new StreamableVideo(title, mp4, mp4Mobile)));
                } else {
                    handler.post(() -> fetchVideoLinkListener.failed(null));
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                handler.post(() -> fetchVideoLinkListener.failed(null));
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
