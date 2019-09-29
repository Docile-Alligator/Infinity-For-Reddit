package ml.docilealligator.infinityforreddit.SubredditDatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
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
    @ColumnInfo(name = "subscribers_count")
    private int nSubscribers;

    public SubredditData(@NonNull String id, String name, String iconUrl, String bannerUrl, String description, int nSubscribers) {
        this.id = id;
        this.name = name;
        this.iconUrl = iconUrl;
        this.bannerUrl = bannerUrl;
        this.description = description;
        this.nSubscribers = nSubscribers;
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

    public int getNSubscribers() {
        return nSubscribers;
    }
}
