package Account;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "username")
    private String username;
    @ColumnInfo(name = "profile_image_url")
    private String profileImageUrl;
    @ColumnInfo(name = "banner_image_url")
    private String bannerImageUrl;
    @ColumnInfo(name = "karma")
    private int karma;
    @ColumnInfo(name = "access_token")
    private String accessToken;
    @ColumnInfo(name = "refresh_token")
    private String refreshToken;
    @ColumnInfo(name = "code")
    private String code;
    @ColumnInfo(name = "is_current_user")
    private boolean isCurrentUser;

    public Account(@NonNull String username, String accessToken, String refreshToken, String code,
                   String profileImageUrl, String bannerImageUrl, int karma, boolean isCurrentUser) {
        this.username = username;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.code = code;
        this.profileImageUrl = profileImageUrl;
        this.bannerImageUrl = bannerImageUrl;
        this.karma = karma;
        this.isCurrentUser = isCurrentUser;
    }

    public String getUsername() {
        return username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public int getKarma() {
        return karma;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getCode() {
        return code;
    }

    public boolean isCurrentUser() {
        return isCurrentUser;
    }
}
