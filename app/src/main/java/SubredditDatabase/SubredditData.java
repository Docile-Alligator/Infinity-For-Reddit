package SubredditDatabase;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.support.annotation.NonNull;

import SubscribedSubredditDatabase.SubscribedSubredditData;

@Entity(tableName = "subreddits")
public class SubredditData extends SubscribedSubredditData {
    @ColumnInfo(name = "banner")
    private String bannerUrl;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "subscribers_count")
    private int nSubscribers;

    public SubredditData(@NonNull String id, String name, String iconUrl, String bannerUrl, String description, int nSubscribers) {
        super(id, name, iconUrl);
        this.bannerUrl = bannerUrl;
        this.description = description;
        this.nSubscribers = nSubscribers;
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
