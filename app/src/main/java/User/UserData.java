package User;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserData {

  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "name")
  private final String name;
  @ColumnInfo(name = "icon")
  private final String iconUrl;
  @ColumnInfo(name = "banner")
  private final String banner;
  @ColumnInfo(name = "karma")
  private final int karma;
  @ColumnInfo(name = "is_gold")
  private final boolean isGold;
  @ColumnInfo(name = "is_friend")
  private final boolean isFriend;
  @ColumnInfo(name = "can_be_followed")
  private final boolean canBeFollowed;

  public UserData(@NonNull String name, String iconUrl, String banner, int karma, boolean isGold,
      boolean isFriend, boolean canBeFollowed) {
    this.name = name;
    this.iconUrl = iconUrl;
    this.banner = banner;
    this.karma = karma;
    this.isGold = isGold;
    this.isFriend = isFriend;
    this.canBeFollowed = canBeFollowed;
  }

  @NonNull
  public String getName() {
    return name;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public String getBanner() {
    return banner;
  }

  public int getKarma() {
    return karma;
  }

  public boolean isGold() {
    return isGold;
  }

  public boolean isFriend() {
    return isFriend;
  }

  public boolean isCanBeFollowed() {
    return canBeFollowed;
  }
}
