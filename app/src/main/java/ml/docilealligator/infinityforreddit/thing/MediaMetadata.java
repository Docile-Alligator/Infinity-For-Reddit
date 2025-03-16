package ml.docilealligator.infinityforreddit.thing;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MediaMetadata implements Parcelable {
    public String id;
    //E.g. Image
    public String e;
    public String fileName;
    public String caption;
    public boolean isGIF;
    public MediaItem original;
    public MediaItem downscaled;

    public MediaMetadata(String id, String e, MediaItem original, MediaItem downscaled) {
        this.id = id;
        this.e = e;
        isGIF = !e.equalsIgnoreCase("image");
        String path = Uri.parse(original.url).getPath();
        this.fileName = path == null ? (isGIF ? "Animated.gif" : "Image.jpg") : path.substring(path.lastIndexOf('/') + 1);
        this.original = original;
        this.downscaled = downscaled;
    }

    protected MediaMetadata(Parcel in) {
        id = in.readString();
        e = in.readString();
        fileName = in.readString();
        caption = in.readString();
        isGIF = in.readByte() != 0;
        original = in.readParcelable(MediaItem.class.getClassLoader());
        downscaled = in.readParcelable(MediaItem.class.getClassLoader());
    }

    public static final Creator<MediaMetadata> CREATOR = new Creator<MediaMetadata>() {
        @Override
        public MediaMetadata createFromParcel(Parcel in) {
            return new MediaMetadata(in);
        }

        @Override
        public MediaMetadata[] newArray(int size) {
            return new MediaMetadata[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(e);
        dest.writeString(fileName);
        dest.writeString(caption);
        dest.writeByte((byte) (isGIF ? 1 : 0));
        dest.writeParcelable(original, flags);
        dest.writeParcelable(downscaled, flags);
    }

    public static class MediaItem implements Parcelable {
        public int x;
        public int y;
        //Image or gif
        public String url;
        //Only for gifs
        @Nullable
        public String mp4Url;

        public MediaItem(int x, int y, String url) {
            this.x = x;
            this.y = y;
            this.url = url;
        }

        public MediaItem(int x, int y, String url, String mp4Url) {
            this.x = x;
            this.y = y;
            this.url = url;
            this.mp4Url = mp4Url;
        }

        protected MediaItem(Parcel in) {
            x = in.readInt();
            y = in.readInt();
            url = in.readString();
            mp4Url = in.readString();
        }

        public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
            @Override
            public MediaItem createFromParcel(Parcel in) {
                return new MediaItem(in);
            }

            @Override
            public MediaItem[] newArray(int size) {
                return new MediaItem[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(x);
            dest.writeInt(y);
            dest.writeString(url);
            dest.writeString(mp4Url);
        }
    }
}
