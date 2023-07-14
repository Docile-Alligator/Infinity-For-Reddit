package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.post.Post;

public class TrendingSearch implements Parcelable {
    public String queryString;
    public String displayString;
    public String title;
    public ArrayList<Post.Preview> previews;

    public TrendingSearch(String queryString, String displayString, String title, ArrayList<Post.Preview> previews) {
        this.queryString = queryString;
        this.displayString = displayString;
        this.title = title;
        this.previews = previews;
    }

    protected TrendingSearch(Parcel in) {
        queryString = in.readString();
        displayString = in.readString();
        title = in.readString();
        previews = in.createTypedArrayList(Post.Preview.CREATOR);
    }

    public static final Creator<TrendingSearch> CREATOR = new Creator<TrendingSearch>() {
        @Override
        public TrendingSearch createFromParcel(Parcel in) {
            return new TrendingSearch(in);
        }

        @Override
        public TrendingSearch[] newArray(int size) {
            return new TrendingSearch[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(queryString);
        parcel.writeString(displayString);
        parcel.writeString(title);
        parcel.writeTypedList(previews);
    }
}
