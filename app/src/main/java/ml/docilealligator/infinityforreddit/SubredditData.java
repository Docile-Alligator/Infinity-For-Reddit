package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

class SubredditData implements Parcelable {
    private String name;
    private String iconUrl;

    SubredditData(String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }

    protected SubredditData(Parcel in) {
        name = in.readString();
        iconUrl = in.readString();
    }

    public static final Creator<SubredditData> CREATOR = new Creator<SubredditData>() {
        @Override
        public SubredditData createFromParcel(Parcel in) {
            return new SubredditData(in);
        }

        @Override
        public SubredditData[] newArray(int size) {
            return new SubredditData[size];
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
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(iconUrl);
    }
}
