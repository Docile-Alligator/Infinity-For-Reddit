package ml.docilealligator.infinityforreddit.User;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserData {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "icon")
    private String iconUrl;
    @ColumnInfo(name = "banner")
    private String banner;
    @ColumnInfo(name = "link_karma")
    private int linkKarma;
    @ColumnInfo(name = "comment_karma")
    private int commentKarma;
    @ColumnInfo(name = "created_utc")
    private long cakeday;
    @ColumnInfo(name = "is_gold")
    private boolean isGold;
    @ColumnInfo(name = "is_friend")
    private boolean isFriend;
    @ColumnInfo(name = "can_be_followed")
    private boolean canBeFollowed;
    @ColumnInfo(name = "description")
    private String description;

    public UserData(@NonNull String name, String iconUrl, String banner, int linkKarma, int commentKarma,
                    long cakeday, boolean isGold, boolean isFriend, boolean canBeFollowed, String description) {
        this.name = name;
        this.iconUrl = iconUrl;
        this.banner = banner;
        this.commentKarma = commentKarma;
        this.linkKarma = linkKarma;
        this.cakeday = cakeday;
        this.isGold = isGold;
        this.isFriend = isFriend;
        this.canBeFollowed = canBeFollowed;
        this.description = description;
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

    public int getLinkKarma() {
        return linkKarma;
    }

    public int getCommentKarma() {
        return commentKarma;
    }

    public int getKarma() {
        return linkKarma + commentKarma;
    }

    public long getCakeday() {
        return cakeday;
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

    public String getDescription() {
        return description;
    }
}
