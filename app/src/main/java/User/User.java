package User;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "user_name")
    private String userName;
    @ColumnInfo(name = "icon")
    private String icon;
    @ColumnInfo(name = "banner")
    private String banner;
    @ColumnInfo(name = "karma")
    private int karma;
    @ColumnInfo(name = "is_gold")
    private boolean isGold;
    @ColumnInfo(name = "is_friend")
    private boolean isFriend;

    User(@NonNull String userName, String icon, String banner, int karma, boolean isGold, boolean isFriend) {
        this.userName = userName;
        this.icon = icon;
        this.banner = banner;
        this.karma = karma;
        this.isGold = isGold;
        this.isFriend = isFriend;
    }

    @NonNull
    public String getUserName() {
        return userName;
    }

    public String getIcon() {
        return icon;
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
}
