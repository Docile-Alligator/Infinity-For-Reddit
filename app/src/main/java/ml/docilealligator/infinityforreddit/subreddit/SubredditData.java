package ml.docilealligator.infinityforreddit.subreddit;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "subreddits")
public class SubredditData implements Parcelable {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private final String id;
    @ColumnInfo(name = "name")
    private final String name;
    @ColumnInfo(name = "icon")
    private final String iconUrl;
    @ColumnInfo(name = "banner")
    private final String bannerUrl;
    @ColumnInfo(name = "description")
    private final String description;
    @ColumnInfo(name = "sidebar_description")
    private final String sidebarDescription;
    @ColumnInfo(name = "subscribers_count")
    private final int nSubscribers;
    @ColumnInfo(name = "created_utc")
    private final long createdUTC;
    @ColumnInfo(name = "suggested_comment_sort")
    private final String suggestedCommentSort;
    @ColumnInfo(name = "over18")
    private final boolean isNSFW;
    @Ignore
    private boolean isSelected;

    public SubredditData(@NonNull String id, String name, String iconUrl, String bannerUrl,
                         String description, String sidebarDescription, int nSubscribers, long createdUTC,
                         String suggestedCommentSort, boolean isNSFW) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.bannerUrl = bannerUrl;
        this.description = description;
        this.sidebarDescription = sidebarDescription;
        this.nSubscribers = nSubscribers;
        this.createdUTC = createdUTC;
        this.suggestedCommentSort = suggestedCommentSort;
        this.isNSFW = isNSFW;
        this.isSelected = false;
    }

    protected SubredditData(Parcel in) {
        id = in.readString();
        name = in.readString();
        iconUrl = in.readString();
        bannerUrl = in.readString();
        description = in.readString();
        sidebarDescription = in.readString();
        nSubscribers = in.readInt();
        createdUTC = in.readLong();
        suggestedCommentSort = in.readString();
        isNSFW = in.readByte() != 0;
        isSelected = in.readByte() != 0;
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

    @NonNull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getSidebarDescription() {
        return sidebarDescription;
    }

    public int getNSubscribers() {
        return nSubscribers;
    }

    public long getCreatedUTC() {
        return createdUTC;
    }

    public String getSuggestedCommentSort() {
        return suggestedCommentSort;
    }

    public boolean isNSFW() {
        return isNSFW;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(iconUrl);
        dest.writeString(bannerUrl);
        dest.writeString(description);
        dest.writeString(sidebarDescription);
        dest.writeInt(nSubscribers);
        dest.writeLong(createdUTC);
        dest.writeString(suggestedCommentSort);
        dest.writeByte((byte) (isNSFW ? 1 : 0));
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }
}
