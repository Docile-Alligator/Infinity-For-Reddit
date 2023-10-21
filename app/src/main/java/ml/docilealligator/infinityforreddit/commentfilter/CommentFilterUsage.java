package ml.docilealligator.infinityforreddit.commentfilter;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "comment_filter_usage", primaryKeys = {"name", "usage", "name_of_usage"},
        foreignKeys = @ForeignKey(entity = CommentFilter.class, parentColumns = "name",
                childColumns = "name", onDelete = ForeignKey.CASCADE))
public class CommentFilterUsage implements Parcelable {
    public static final int SUBREDDIT_TYPE = 1;

    @NonNull
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "usage")
    public int usage;
    @NonNull
    @ColumnInfo(name = "name_of_usage")
    public String nameOfUsage;

    public CommentFilterUsage(@NonNull String name, int usage, @NonNull String nameOfUsage) {
        this.name = name;
        this.usage = usage;
        this.nameOfUsage = nameOfUsage;
    }

    protected CommentFilterUsage(Parcel in) {
        name = in.readString();
        usage = in.readInt();
        nameOfUsage = in.readString();
    }

    public static final Creator<CommentFilterUsage> CREATOR = new Creator<CommentFilterUsage>() {
        @Override
        public CommentFilterUsage createFromParcel(Parcel in) {
            return new CommentFilterUsage(in);
        }

        @Override
        public CommentFilterUsage[] newArray(int size) {
            return new CommentFilterUsage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(usage);
        dest.writeString(nameOfUsage);
    }
}
