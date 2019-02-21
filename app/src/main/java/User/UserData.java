package User;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.annotation.NonNull;

import SubscribedUserDatabase.SubscribedUserData;

@Entity(tableName = "users")
public class UserData extends SubscribedUserData {
    @ColumnInfo(name = "banner")
    private String banner;
    @ColumnInfo(name = "karma")
    private int karma;
    @ColumnInfo(name = "is_gold")
    private boolean isGold;
    @ColumnInfo(name = "is_friend")
    private boolean isFriend;
    @ColumnInfo(name = "can_be_followed")
    private boolean canBeFollowed;

    public UserData(@NonNull String name, String iconUrl, String banner, int karma, boolean isGold, boolean isFriend, boolean canBeFollowed) {
        super(name, iconUrl);
        this.banner = banner;
        this.karma = karma;
        this.isGold = isGold;
        this.isFriend = isFriend;
        this.canBeFollowed = canBeFollowed;
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
