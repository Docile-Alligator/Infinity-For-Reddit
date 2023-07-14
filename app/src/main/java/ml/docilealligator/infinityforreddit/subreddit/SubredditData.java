package ml.docilealligator.infinityforreddit.subreddit;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "subreddits")
public class SubredditData {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "icon")
    private String iconUrl;
    @ColumnInfo(name = "banner")
    private String bannerUrl;
    @ColumnInfo(name = "description")
    private String description;
    @ColumnInfo(name = "sidebar_description")
    private String sidebarDescription;
    @ColumnInfo(name = "subscribers_count")
    private int nSubscribers;
    @ColumnInfo(name = "created_utc")
    private long createdUTC;
    @ColumnInfo(name = "suggested_comment_sort")
    private String suggestedCommentSort;
    @ColumnInfo(name = "over18")
    private boolean isNSFW;
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
}
