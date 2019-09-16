package SubscribedUserDatabase;

import Account.Account;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "subscribed_users", foreignKeys = @ForeignKey(entity = Account.class, parentColumns = "username",
    childColumns = "username", onDelete = ForeignKey.CASCADE))
public class SubscribedUserData {

  @PrimaryKey
  @NonNull
  @ColumnInfo(name = "name")
  private final String name;
  @ColumnInfo(name = "icon")
  private final String iconUrl;
  @ColumnInfo(name = "username")
  private String username;

  public SubscribedUserData(@NonNull String name, String iconUrl, String username) {
    this.name = name;
    this.iconUrl = iconUrl;
    this.username = username;
  }

  @NonNull
  public String getName() {
    return name;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }
}
