package ml.docilealligator.infinityforreddit.multireddit;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.account.Account;

@Entity(tableName = "multi_reddits", primaryKeys = {"path", "username"},
        foreignKeys = @ForeignKey(entity = Account.class, parentColumns = "username",
                childColumns = "username", onDelete = ForeignKey.CASCADE))
public class MultiReddit implements Parcelable {
    @NonNull
    @ColumnInfo(name = "path")
    private String path;
    @NonNull
    @ColumnInfo(name = "display_name")
    private String displayName;
    @NonNull
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "copied_from")
    private String copiedFrom;
    @ColumnInfo(name = "icon_url")
    private String iconUrl;
    @ColumnInfo(name = "visibility")
    private String visibility;
    @NonNull
    @ColumnInfo(name = "username")
    private String owner;
    @ColumnInfo(name = "n_subscribers")
    private int nSubscribers;
    @ColumnInfo(name = "created_UTC")
    private long createdUTC;
    @ColumnInfo(name = "over_18")
    private boolean over18;
    @ColumnInfo(name = "is_subscriber")
    private boolean isSubscriber;
    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;
    @Ignore
    private ArrayList<String> subreddits;

    public MultiReddit(@NonNull String path, @NonNull String displayName, @NonNull String name,
                       String description, String copiedFrom, String iconUrl, String visibility,
                       @NonNull String owner, int nSubscribers, long createdUTC, boolean over18,
                       boolean isSubscriber, boolean isFavorite) {
        this.displayName = displayName;
        this.name = name;
        this.description = description;
        this.copiedFrom = copiedFrom;
        this.iconUrl = iconUrl;
        this.visibility = visibility;
        this.path = path;
        this.owner = owner;
        this.nSubscribers = nSubscribers;
        this.createdUTC = createdUTC;
        this.over18 = over18;
        this.isSubscriber = isSubscriber;
        this.isFavorite = isFavorite;
    }

    public MultiReddit(@NonNull String path, @NonNull String displayName, @NonNull String name,
                       String description, String copiedFrom, String iconUrl, String visibility,
                       @NonNull String owner, int nSubscribers, long createdUTC, boolean over18,
                       boolean isSubscriber, boolean isFavorite, ArrayList<String> subreddits) {
        this.displayName = displayName;
        this.name = name;
        this.description = description;
        this.copiedFrom = copiedFrom;
        this.iconUrl = iconUrl;
        this.visibility = visibility;
        this.path = path;
        this.owner = owner;
        this.nSubscribers = nSubscribers;
        this.createdUTC = createdUTC;
        this.over18 = over18;
        this.isSubscriber = isSubscriber;
        this.isFavorite = isFavorite;
        this.subreddits = subreddits;
    }

    protected MultiReddit(Parcel in) {
        path = in.readString();
        displayName = in.readString();
        name = in.readString();
        description = in.readString();
        copiedFrom = in.readString();
        iconUrl = in.readString();
        visibility = in.readString();
        owner = in.readString();
        nSubscribers = in.readInt();
        createdUTC = in.readLong();
        over18 = in.readByte() != 0;
        isSubscriber = in.readByte() != 0;
        isFavorite = in.readByte() != 0;
        subreddits = new ArrayList<>();
        in.readStringList(subreddits);
    }

    public static final Creator<MultiReddit> CREATOR = new Creator<>() {
        @Override
        public MultiReddit createFromParcel(Parcel in) {
            return new MultiReddit(in);
        }

        @Override
        public MultiReddit[] newArray(int size) {
            return new MultiReddit[size];
        }
    };

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NonNull String displayName) {
        this.displayName = displayName;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCopiedFrom() {
        return copiedFrom;
    }

    public void setCopiedFrom(String copiedFrom) {
        this.copiedFrom = copiedFrom;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getNSubscribers() {
        return nSubscribers;
    }

    public void setNSubscribers(int nSubscribers) {
        this.nSubscribers = nSubscribers;
    }

    public long getCreatedUTC() {
        return createdUTC;
    }

    public void setCreatedUTC(long createdUTC) {
        this.createdUTC = createdUTC;
    }

    public boolean isOver18() {
        return over18;
    }

    public void setOver18(boolean over18) {
        this.over18 = over18;
    }

    public boolean isSubscriber() {
        return isSubscriber;
    }

    public void setSubscriber(boolean subscriber) {
        isSubscriber = subscriber;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public ArrayList<String> getSubreddits() {
        return subreddits;
    }

    public void setSubreddits(ArrayList<String> subreddits) {
        this.subreddits = subreddits;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(path);
        parcel.writeString(displayName);
        parcel.writeString(name);
        parcel.writeString(description);
        parcel.writeString(copiedFrom);
        parcel.writeString(iconUrl);
        parcel.writeString(visibility);
        parcel.writeString(owner);
        parcel.writeInt(nSubscribers);
        parcel.writeLong(createdUTC);
        parcel.writeByte((byte) (over18 ? 1 : 0));
        parcel.writeByte((byte) (isSubscriber ? 1 : 0));
        parcel.writeByte((byte) (isFavorite ? 1 : 0));
        parcel.writeStringList(subreddits);
    }
}
