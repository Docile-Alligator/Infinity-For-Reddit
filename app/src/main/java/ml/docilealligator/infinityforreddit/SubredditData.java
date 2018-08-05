package ml.docilealligator.infinityforreddit;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "subreddits")
class SubredditData {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "icon_url")
    private String iconUrl;

    @ColumnInfo(name = "banner_url")
    private String bannerUrl;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "subscribers_count")
    private int nSubscribers;

    SubredditData(@NonNull String id, String name, String iconUrl, String bannerUrl, String description, int nSubscribers) {
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
