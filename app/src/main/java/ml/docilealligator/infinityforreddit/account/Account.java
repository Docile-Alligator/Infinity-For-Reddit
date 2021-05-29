package ml.docilealligator.infinityforreddit.account;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "accounts")
public class Account {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "username")
    private String accountName;
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

    @Ignore
    public static Account getAnonymousAccount() {
        return new Account("-", null, null, null, null, null, 0, false);
    }

    public Account(@NonNull String accountName, String accessToken, String refreshToken, String code,
                   String profileImageUrl, String bannerImageUrl, int karma, boolean isCurrentUser) {
        this.accountName = accountName;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.code = code;
        this.profileImageUrl = profileImageUrl;
        this.bannerImageUrl = bannerImageUrl;
        this.karma = karma;
        this.isCurrentUser = isCurrentUser;
    }

    @NonNull
    public String getAccountName() {
        return accountName;
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

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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
