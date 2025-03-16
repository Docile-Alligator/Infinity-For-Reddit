package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.annotation.WorkerThread;
import androidx.media3.common.util.UnstableApi;

import org.apache.commons.io.FilenameUtils;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Provider;

import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.apis.StreamableAPI;
import ml.docilealligator.infinityforreddit.apis.VReddIt;
import ml.docilealligator.infinityforreddit.post.FetchPost;
import ml.docilealligator.infinityforreddit.post.FetchStreamableVideo;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.thing.FetchRedgifsVideoLinks;
import ml.docilealligator.infinityforreddit.thing.StreamableVideo;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class VideoLinkFetcher {
    public static void fetchVideoLink(Executor executor, Handler handler, Retrofit retrofit, Retrofit vReddItRetrofit,
                                      Retrofit redgifsRetrofit, Provider<StreamableAPI> streamableApiProvider,
                                      SharedPreferences currentAccountSharedPreferences, int videoType,
                                      @Nullable String redgifsId, @Nullable String vRedditItUrl,
                                      @Nullable String shortCode,
                                      FetchVideoLinkListener fetchVideoLinkListener) {
        switch (videoType) {
            case ViewVideoActivity.VIDEO_TYPE_STREAMABLE:
                FetchStreamableVideo.fetchStreamableVideo(executor, handler, streamableApiProvider, shortCode, fetchVideoLinkListener);
                break;
            case ViewVideoActivity.VIDEO_TYPE_REDGIFS:
                FetchRedgifsVideoLinks.fetchRedgifsVideoLinks(executor, handler, redgifsRetrofit,
                        currentAccountSharedPreferences, redgifsId, fetchVideoLinkListener);
                break;
            case ViewVideoActivity.VIDEO_TYPE_V_REDD_IT:
                loadVReddItVideo(executor, handler, retrofit, vReddItRetrofit, redgifsRetrofit, streamableApiProvider,
                        currentAccountSharedPreferences, vRedditItUrl, fetchVideoLinkListener);
                break;
        }
    }

    @WorkerThread
    @Nullable
    public static String fetchVideoLinkSync(Retrofit redgifsRetrofit, Provider<StreamableAPI> streamableApiProvider,
                                      SharedPreferences currentAccountSharedPreferences, int videoType,
                                      @Nullable String redgifsId, @Nullable String shortCode) {
        if (videoType == ViewVideoActivity.VIDEO_TYPE_STREAMABLE) {
            StreamableVideo streamableVideo = FetchStreamableVideo.fetchStreamableVideoSync(streamableApiProvider, shortCode);
            return streamableVideo == null ? null : (streamableVideo.mp4 == null ? null : streamableVideo.mp4.url);
        } else if (videoType == ViewVideoActivity.VIDEO_TYPE_REDGIFS) {
            return FetchRedgifsVideoLinks.fetchRedgifsVideoLinkSync(redgifsRetrofit,
                    currentAccountSharedPreferences, redgifsId);
        }

        return null;
    }

    public static void loadVReddItVideo(Executor executor, Handler handler, Retrofit retrofit, Retrofit mVReddItRetrofit,
                                        Retrofit redgifsRetrofit, Provider<StreamableAPI> streamableApiProvider,
                                        SharedPreferences currentAccountSharedPreferences,
                                        String vRedditItUrl, FetchVideoLinkListener fetchVideoLinkListener) {
        mVReddItRetrofit.create(VReddIt.class).getRedirectUrl(vRedditItUrl).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    Uri redirectUri = Uri.parse(response.raw().request().url().toString());
                    String redirectPath = redirectUri.getPath();
                    if (redirectPath != null && (redirectPath.matches("/r/\\w+/comments/\\w+/?\\w+/?") || redirectPath.matches("/user/\\w+/comments/\\w+/?\\w+/?"))) {
                        List<String> segments = redirectUri.getPathSegments();
                        int commentsIndex = segments.lastIndexOf("comments");
                        String postId = segments.get(commentsIndex + 1);
                        FetchPost.fetchPost(executor, handler, retrofit, postId, null, Account.ANONYMOUS_ACCOUNT,
                                new FetchPost.FetchPostListener() {
                                    @OptIn(markerClass = UnstableApi.class)
                                    @Override
                                    public void fetchPostSuccess(Post post) {
                                        fetchVideoLinkListener.onFetchVideoFallbackDirectUrlSuccess(post.getVideoFallBackDirectUrl());
                                        if (post.isRedgifs()) {
                                            String redgifsId = post.getRedgifsId();
                                            if (redgifsId != null && redgifsId.contains("-")) {
                                                redgifsId = redgifsId.substring(0, redgifsId.indexOf('-'));
                                            }
                                            fetchVideoLinkListener.onChangeFileName("Redgifs-" + redgifsId + ".mp4");

                                            FetchRedgifsVideoLinks.fetchRedgifsVideoLinks(executor, handler, redgifsRetrofit,
                                                    currentAccountSharedPreferences, redgifsId, fetchVideoLinkListener);
                                        } else if (post.isStreamable()) {
                                            String shortCode = post.getStreamableShortCode();
                                            fetchVideoLinkListener.onChangeFileName("Streamable-" + shortCode + ".mp4");

                                            FetchStreamableVideo.fetchStreamableVideo(executor, handler, streamableApiProvider, shortCode, fetchVideoLinkListener);
                                        } else if (post.isImgur()) {
                                            String videoDownloadUrl = post.getVideoDownloadUrl();
                                            String videoFileName = "Imgur-" + FilenameUtils.getName(videoDownloadUrl);
                                            fetchVideoLinkListener.onFetchImgurVideoLinkSuccess(post.getVideoUrl(), post.getVideoDownloadUrl(), videoFileName);
                                        } else {
                                            if (post.getVideoUrl() != null) {
                                                String videoFileName = post.getSubredditName() + "-" + post.getId() + ".mp4";
                                                fetchVideoLinkListener.onFetchRedditVideoLinkSuccess(post, videoFileName);
                                            } else {
                                                fetchVideoLinkListener.failed(R.string.error_fetching_v_redd_it_video_cannot_get_video_url);
                                            }
                                        }
                                    }

                                    @Override
                                    public void fetchPostFailed() {
                                        fetchVideoLinkListener.failed(R.string.error_fetching_v_redd_it_video_cannot_get_post);
                                    }
                                });
                    } else {
                        fetchVideoLinkListener.failed(R.string.error_fetching_v_redd_it_video_cannot_get_post_id);
                    }
                } else {
                    fetchVideoLinkListener.failed(R.string.error_fetching_v_redd_it_video_cannot_get_redirect_url);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchVideoLinkListener.failed(R.string.error_fetching_v_redd_it_video_cannot_get_redirect_url);
            }
        });
    }
}
