package ml.docilealligator.infinityforreddit.multireddit;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class ExpandedSubredditInMultiReddit implements Parcelable {
    private String name;
    private String iconUrl;

    public ExpandedSubredditInMultiReddit(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }

    protected ExpandedSubredditInMultiReddit(Parcel in) {
        name = in.readString();
        iconUrl = in.readString();
    }

    public static final Creator<ExpandedSubredditInMultiReddit> CREATOR = new Creator<>() {
        @Override
        public ExpandedSubredditInMultiReddit createFromParcel(Parcel in) {
            return new ExpandedSubredditInMultiReddit(in);
        }

        @Override
        public ExpandedSubredditInMultiReddit[] newArray(int size) {
            return new ExpandedSubredditInMultiReddit[size];
        }
    };

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(iconUrl);
    }
}
