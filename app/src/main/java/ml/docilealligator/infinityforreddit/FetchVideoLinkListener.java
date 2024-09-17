package ml.docilealligator.infinityforreddit;

import androidx.annotation.Nullable;

import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.thing.StreamableVideo;

public interface FetchVideoLinkListener {
    default void onFetchRedditVideoLinkSuccess(Post post, String fileName) {}
    default void onFetchImgurVideoLinkSuccess(String videoUrl, String videoDownloadUrl, String fileName) {}
    default void onFetchRedgifsVideoLinkSuccess(String webm, String mp4) {}
    default void onFetchStreamableVideoLinkSuccess(StreamableVideo streamableVideo) {}
    default void onChangeFileName(String fileName) {}
    default void onFetchVideoFallbackDirectUrlSuccess(String videoFallbackDirectUrl) {}
    default void failed(@Nullable Integer messageRes) {}
}
