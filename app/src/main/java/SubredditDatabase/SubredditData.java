package SubredditDatabase;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "subreddits")
public class SubredditData {

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
  @ColumnInfo(name = "subscribers_count")
  private final int nSubscribers;

  public SubredditData(@NonNull String id, String name, String iconUrl, String bannerUrl,
      String description, int nSubscribers) {
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
