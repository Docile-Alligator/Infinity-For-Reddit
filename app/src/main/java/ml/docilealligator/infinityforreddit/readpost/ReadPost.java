package ml.docilealligator.infinityforreddit.readpost;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

import ml.docilealligator.infinityforreddit.account.Account;

@Entity(tableName = "read_posts", primaryKeys = {"username", "id"},
        foreignKeys = @ForeignKey(entity = Account.class, parentColumns = "username",
                childColumns = "username", onDelete = ForeignKey.CASCADE))
public class ReadPost implements Parcelable {
    @NonNull
    @ColumnInfo(name = "username")
    private String username;
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    public ReadPost(@NonNull String username, @NonNull String id) {
        this.username = username;
        this.id = id;
    }

    protected ReadPost(Parcel in) {
        username = in.readString();
        id = in.readString();
    }

    public static final Creator<ReadPost> CREATOR = new Creator<ReadPost>() {
        @Override
        public ReadPost createFromParcel(Parcel in) {
            return new ReadPost(in);
        }

        @Override
        public ReadPost[] newArray(int size) {
            return new ReadPost[size];
        }
    };

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(username);
        parcel.writeString(id);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof ReadPost) {
            return ((ReadPost) obj).id.equals(id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
