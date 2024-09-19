package ml.docilealligator.infinityforreddit.thing;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class GiphyGif implements Parcelable {
    public final String id;
    private GiphyGif(String id) {
        this.id = id;
    }

    public GiphyGif(String id, boolean modifyId) {
        this(modifyId ? "giphy|" + id + "|downsized" : id);
    }

    protected GiphyGif(Parcel in) {
        id = in.readString();
    }

    public static final Creator<GiphyGif> CREATOR = new Creator<GiphyGif>() {
        @Override
        public GiphyGif createFromParcel(Parcel in) {
            return new GiphyGif(in);
        }

        @Override
        public GiphyGif[] newArray(int size) {
            return new GiphyGif[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
    }
}
