package ml.docilealligator.infinityforreddit.postfilter;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "post_filter_usage", primaryKeys = {"name", "usage", "name_of_usage"},
        foreignKeys = @ForeignKey(entity = PostFilter.class, parentColumns = "name",
                childColumns = "name", onDelete = ForeignKey.CASCADE))
public class PostFilterUsage implements Parcelable {
    public static final int HOME_TYPE = 1;
    public static final int SUBREDDIT_TYPE = 2;
    public static final int USER_TYPE = 3;
    public static final int MULTIREDDIT_TYPE = 4;
    public static final int SEARCH_TYPE = 5;
    public static final String NO_USAGE = "--";

    @NonNull
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "usage")
    public int usage;
    @NonNull
    @ColumnInfo(name = "name_of_usage")
    public String nameOfUsage;

    public PostFilterUsage(@NonNull String name, int usage, String nameOfUsage) {
        this.name = name;
        this.usage = usage;
        if (nameOfUsage == null || nameOfUsage.equals("")) {
            this.nameOfUsage = NO_USAGE;
        } else {
            this.nameOfUsage = nameOfUsage;
        }
    }

    protected PostFilterUsage(Parcel in) {
        name = in.readString();
        usage = in.readInt();
        nameOfUsage = in.readString();
    }

    public static final Creator<PostFilterUsage> CREATOR = new Creator<PostFilterUsage>() {
        @Override
        public PostFilterUsage createFromParcel(Parcel in) {
            return new PostFilterUsage(in);
        }

        @Override
        public PostFilterUsage[] newArray(int size) {
            return new PostFilterUsage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(usage);
        parcel.writeString(nameOfUsage);
    }
}
