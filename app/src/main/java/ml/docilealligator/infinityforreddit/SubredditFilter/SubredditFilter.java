package ml.docilealligator.infinityforreddit.SubredditFilter;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "subreddit_filter", primaryKeys = {"subreddit_name", "type"})
public class SubredditFilter implements Parcelable {
    public static int TYPE_POPULAR_AND_ALL = 0;
    @NonNull
    @ColumnInfo(name = "subreddit_name")
    private String subredditName;
    @ColumnInfo(name = "type")
    private int type;

    public SubredditFilter(@NonNull String subredditName, int type) {
        this.subredditName = subredditName;
        this.type = type;
    }

    protected SubredditFilter(Parcel in) {
        subredditName = in.readString();
        type = in.readInt();
    }

    public static final Creator<SubredditFilter> CREATOR = new Creator<SubredditFilter>() {
        @Override
        public SubredditFilter createFromParcel(Parcel in) {
            return new SubredditFilter(in);
        }

        @Override
        public SubredditFilter[] newArray(int size) {
            return new SubredditFilter[size];
        }
    };

    @NonNull
    public String getSubredditName() {
        return subredditName;
    }

    public void setSubredditName(@NonNull String subredditName) {
        this.subredditName = subredditName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(subredditName);
        parcel.writeInt(type);
    }
}
