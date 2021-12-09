package ml.docilealligator.infinityforreddit;

import androidx.annotation.Nullable;

public class StreamableVideo {
    public String title;
    @Nullable
    public Media mp4;
    @Nullable
    public Media mp4Mobile;

    public StreamableVideo(String title, @Nullable Media mp4, @Nullable Media mp4Mobile) {
        this.title = title;
        this.mp4 = mp4;
        this.mp4Mobile = mp4Mobile;
    }

    public static class Media {
        public String url;
        public int width;
        public int height;

        public Media(String url, int width, int height) {
            this.url = url;
            this.width = width;
            this.height = height;
        }
    }
}
